package com.blinkbox.books.messaging

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader}
import java.nio.charset.{Charset, StandardCharsets}
import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.blinkbox.books.json.{DefaultFormats, ExplicitTypeHints}
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.jackson.Serialization

import scala.concurrent.{ExecutionContext, Future}

/**
 * Values describing what operation an event relates to, for logging, tracing etc.
 *
 * NOTE: This is work in progress, and not the final set of values.
 */
final case class EventHeader(
  id: String,
  timestamp: DateTime,
  originator: String,
  userId: Option[String],
  transactionId: Option[String])

object EventHeader {

  /** Create event header with given values, and timestamp set to the current time. */
  def apply(originator: String, userId: Option[String], transactionId: Option[String], id: String = generateId()): EventHeader =
    EventHeader(id, DateTime.now(DateTimeZone.UTC), originator, userId, transactionId)

  /** Create event context without optional values, and timestamp set to the current time. */
  def apply(originator: String): EventHeader = EventHeader(generateId(), DateTime.now(DateTimeZone.UTC), originator, None, None)

  /** Generates a unique identifier for a message. */
  def generateId(): String = UUID.randomUUID().toString
}

/**
 * Allows an object to provide evidence that a case class can be used as a JSON event body
 *
 * Each JSON message is published with a particular media type so that its semantics are well
 * understood by the receiver. This interface should be implemented in an implicit companion
 * object to provide the media type used for the JSON message.
 *
 * {{{
 * case class MyEvent(foo: String, bar: Int)
 *
 * implicit object MyEvent extends JsonEventBody[MyEvent] {
 *   val jsonMediaType = MediaType("application/vnd.blinkbox.books.events.myevent.v1+json")
 * }
 * }}}
 *
 * @tparam T The event body class to provide evidence for.
 */
trait JsonEventBody[T] {
  /**
   * The media type of the JSON body. This should use the `application` main type, and the subtype should
   * conform to the pattern `vnd.blinkbox.books.{schemaName}+json`, where the `{schemaName}` placeholder is
   * the value used for the `$schema` field in the message.
   */
  val jsonMediaType: MediaType
}

/**
 * Helper object for creating and parsing JSON event bodies.
 */
object JsonEventBody {
  private val SchemaNameMatcher = """vnd\.blinkbox\.books\.(.+)\+json""".r

  private implicit val formats = new DefaultFormats {
    override val typeHintFieldName: String = "$schema"
  }

  private val charset = StandardCharsets.UTF_8

  /**
   * Serializes an object to a JSON event body.
   */
  def apply[T <: AnyRef : JsonEventBody](content: T): EventBody = {
    val mediaType = implicitly[JsonEventBody[T]].jsonMediaType
    val schemaTypeHint = ExplicitTypeHints(Map(content.getClass -> schemaName(mediaType)))
    val stream = new ByteArrayOutputStream
    Serialization.write(content, stream)(formats + schemaTypeHint)
    EventBody(stream.toByteArray, mediaType.withCharset(charset))
  }

  /**
   * Deserializes an event body into an object if the media type matches.
   *
   * This method can be used directly to conditionally deserialize an event body, however it may be
   * cleaner to wrap it in a type-specific `unapply` method, for example:
   *
   * {{{
   * case class MyEvent(foo: String, bar: Int)
   *
   * implicit object MyEvent extends JsonEventBody[MyEvent] {
   *   val jsonMediaType = MediaType("application/vnd.blinkbox.books.events.myevent.v1+json")
   *   def unapply(body: EventBody): Option[(String, Int)] = JsonEventBody.unapply[MyEvent](body).flatMap(MyEvent.unapply)
   * }
   * }}}
   *
   * This then allows simple destructuring of an [[EventBody]], for example:
   *
   * {{{
   * val body: EventBody = JsonEventBody(MyEvent("hello", 123))
   * body match {
   *   case MyEvent(foo, bar) => println(s"foo = $foo, bar = $bar")
   * }
   * }}}
   */
  def unapply[T : Manifest : JsonEventBody](body: EventBody): Option[T] = {
    if (body.contentType.mediaType == implicitly[JsonEventBody[T]].jsonMediaType) {
      val reader = new InputStreamReader(new ByteArrayInputStream(body.content), body.contentType.charset.getOrElse(charset))
      Some(Serialization.read[T](reader))
    } else None
  }

  private def schemaName(mediaType: MediaType): String = mediaType.subType match {
    case SchemaNameMatcher(schemaName) => schemaName
    case _ => throw new IllegalArgumentException("The media type does not conform to the expected pattern.")
  }
}

/**
 * Payload of events.
 */
final case class EventBody(
  content: Array[Byte],
  contentType: ContentType) {
  def asString() = new String(content, contentType.charset.getOrElse(StandardCharsets.UTF_8))
}

/**
 * Media type for message payloads.
 * @param mainType The main type, e.g. 'application'.
 * @param subType The subtype, e.g. 'rss+xml'.
 */
final case class MediaType(mainType: String, subType: String) {
  override def toString = s"$mainType/$subType"
  def withCharset(charset: Charset): ContentType = ContentType(this, Some(charset))
}

object MediaType {
  private val MediaTypeRegex = """(application|audio|example|image|message|model|multipart|text|video)/([^/;]+)""".r

  def apply(mediaType: String): MediaType = mediaType match {
    case MediaTypeRegex(mainType, subType) => MediaType(mainType, subType)
    case _ => throw new IllegalArgumentException(s"Invalid media type: $mediaType")
  }
}

/**
 * Content type for message payloads.
 */
final case class ContentType(
  mediaType: MediaType,
  charset: Option[Charset])

object ContentType {
  val XmlContentType = ContentType(MediaType("application/xml"), None)
}

/**
 * This class represents an event as published by a service, to be processed by
 * other services.
 */
final case class Event(
  header: EventHeader,
  body: EventBody)

object Event {

  /** Convenience method for creating event with content as XML. */
  def xml(content: String, header: EventHeader) =
    this(header, EventBody(content.getBytes(StandardCharsets.UTF_8), ContentType.XmlContentType))

  /** Convenience method for creating event with content serialized to JSON. */
  def json[C <: AnyRef : JsonEventBody](header: EventHeader, content: C): Event = Event(header, JsonEventBody(content))
}

/**
 * Common interface for objects that dispose of events that can't be processed,
 * typically because they are invalid.
 *
 *  Implementations will normally persist these events in a safe location, e.g. a database or a DLQ.
 */
trait ErrorHandler {

  /**
   * Record the fact that the given event could not be processed, due to the given reason.
   *
   * May return a failure if the implementation is unable to store the event.
   */
  def handleError(event: Event, error: Throwable): Future[Unit]

}

/**
 * Simple class that delegates error handling to an Akka actor.
 * This will typically be used with an actor that writes the event to persistent storage,
 * for example to a DLQ.
 */
class ActorErrorHandler(delegate: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) extends ErrorHandler with StrictLogging {

  override def handleError(event: Event, e: Throwable): Future[Unit] = {
    logger.error(s"Unrecoverable error in processing event: $event", e)
    (delegate ? event).map(result => ())
  }

}
