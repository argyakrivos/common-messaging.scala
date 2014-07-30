package com.blinkbox.books.messaging

import java.io.ByteArrayOutputStream
import java.nio.charset.{Charset, StandardCharsets}
import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, CustomSerializer}
import org.json4s.JsonAST.{JNull, JString}
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
 * Payload of events.
 */
final case class EventBody(
  content: Array[Byte],
  contentType: ContentType) {
  def asString() = new String(content, contentType.charset.getOrElse(StandardCharsets.UTF_8))
}

/**
 * Provides helper methods to construct event bodies.
 */
object EventBody {
  private object Json {
    case object ISODateTimeSerializer extends CustomSerializer[DateTime](_ => ({
      case JString(s) => ISODateTimeFormat.dateTime.parseDateTime(s)
      case JNull => null
    }, {
      case d: DateTime => JString(ISODateTimeFormat.dateTime.print(d))
    }))

    private implicit val formats = DefaultFormats + ISODateTimeSerializer

    def write[T <: AnyRef](message: T): Array[Byte] = {
      val stream = new ByteArrayOutputStream
      Serialization.write(message, stream)
      stream.toByteArray
    }
  }

  def json[C <: AnyRef](content: C, mediaType: MediaType): EventBody =
    EventBody(Json.write(content), mediaType.withCharset(StandardCharsets.UTF_8))
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
  import scala.language.implicitConversions

  private val MediaTypeRegex = """(application|audio|example|image|message|model|multipart|text|video)/([^/]+)""".r

  def apply(mediaType: String): MediaType = mediaType match {
    case MediaTypeRegex(mainType, subType) => MediaType(mainType, subType)
    case _ => throw new IllegalArgumentException(s"Invalid media type: $mediaType")
  }

  implicit def string2mediaType(mediaType: String) = MediaType(mediaType)
}

/**
 * Content type for message payloads.
 */
final case class ContentType(
  mediaType: MediaType,
  charset: Option[Charset])

object ContentType {
  val XmlContentType = ContentType("application/xml", None)
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
  def json[C <: AnyRef](header: EventHeader, content: C, mediaType: MediaType): Event =
    this(header, EventBody.json(content, mediaType))
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
class ActorErrorHandler(delegate: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) extends ErrorHandler with Logging {

  override def handleError(event: Event, e: Throwable): Future[Unit] = {
    logger.error(s"Unrecoverable error in processing event: $event", e)
    (delegate ? event).map(result => ())
  }

}
