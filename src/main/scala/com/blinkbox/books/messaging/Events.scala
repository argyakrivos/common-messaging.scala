package com.blinkbox.books.messaging

import java.nio.charset.{ Charset, StandardCharsets }
import org.joda.time.{ DateTime, DateTimeZone }
import scala.concurrent.Future

/**
 * Values describing what operation an event relates to, for logging, tracing etc.
 *
 * NOTE: This is work in progress, and not the final set of values.
 */
final case class EventHeader(
  timestamp: DateTime,
  originator: String,
  userId: Option[String],
  transactionId: Option[String])

object EventHeader {

  /** Create event header with given values, and timestamp set to the current time. */
  def apply(originator: String, userId: Option[String], transactionId: Option[String]): EventHeader =
    EventHeader(DateTime.now, originator, userId, transactionId)

  /** Create event context without optional values, and timestamp set to the current time. */
  def apply(originator: String): EventHeader = EventHeader(DateTime.now(DateTimeZone.UTC), originator, None, None)

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
 * Content type for message payloads.
 */
final case class ContentType(
  mediaType: String,
  charset: Option[Charset])

object ContentType {
  val XmlContentType = ContentType("application/xml", None)
  val JsonContentType = ContentType("application/json", Some(StandardCharsets.UTF_8))
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

  /** Convenience method for creating event with content as JSON. */
  def json(content: String, header: EventHeader) =
    this(header, EventBody(content.getBytes(StandardCharsets.UTF_8), ContentType.JsonContentType))

}

/**
 * Common interface used for publishing events.
 */
trait EventPublisher {

  /**
   * Publish an event.
   */
  def publish(event: Event): Future[Unit]
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
