package com.blinkbox.books.messaging

import java.nio.charset.Charset
import org.joda.time.DateTime
import scala.concurrent.Future

/**
 * Values describing what operation an event relates to, for logging, tracing etc.
 *
 * NOTE: This is work in progress, and not the final set of values.
 */
final case class EventContext(
  timestamp: DateTime,
  originator: String,
  userId: Option[String],
  transactionId: Option[String],
  isbn: Option[String]) {
}

object EventContext {
  def apply(originator: String): EventContext = EventContext(DateTime.now, originator, None, None, None)
  def apply(originator: String, userId: Option[String], transactionId: Option[String], isbn: Option[String]): EventContext =
    EventContext(DateTime.now, originator, userId, transactionId, isbn)
}

final case class ContentType(
  mediaType: String,
  charset: Option[Charset])

object ContentType {
  val UTF8 = Charset.forName("UTF-8")
  val XmlContentType = ContentType("application/xml", None)
  // TODO: etc...
}

/**
 * This class represents an event as published by a service, to be processed by
 * other services.
 */
final case class Event(
  body: Array[Byte],
  contentType: ContentType,
  context: EventContext) {

  /** Convenience method for creating event with content as XML, the most common message format. */
  def this(body: String, context: EventContext) =
    this(body.getBytes(ContentType.UTF8), ContentType.XmlContentType, context)

  /** Convenience method for getting content of event as a String, assuming UTF-8 encoding. */
  def contentAsString: String = new String(body, ContentType.UTF8)

}

object Event {
  def apply(body: String, context: EventContext): Event =
    Event(body.getBytes(ContentType.UTF8), ContentType.XmlContentType, context)
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
