package com.blinkbox.books.messaging

import java.io.ByteArrayInputStream
import java.net.URI
import java.nio.charset.StandardCharsets

import com.blinkbox.books.json.DefaultFormats
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.{JNothing, JString}
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatest.FunSuite

object JsonEventBodyTests {
  case class TestEvent1(foo: String)
  case class TestEvent2(bar: DateTime)
  case class URITestObject(address: URI)

  implicit object TestEvent1 extends JsonEventBody[TestEvent1] {
    val jsonMediaType = MediaType("application/vnd.blinkbox.books.events.testevent.v1+json")
  }

  implicit object TestEvent2 extends JsonEventBody[TestEvent2] {
    val jsonMediaType = MediaType("application/vnd.blinkbox.books.events.testevent.v2+json")
  }

  implicit object URITestObject extends JsonEventBody[URITestObject] {
    val jsonMediaType = MediaType("application/vnd.blinkbox.books.events.testevent.v2+json")
  }

  def jsonEvent(json: JValue = JNothing, mediaType: String = "application/vnd.blinkbox.books.events.testevent.v2+json") = {
    implicit object JsonEvent extends JsonEventBody[JValue] {
      val jsonMediaType = MediaType(mediaType)
    }
    Event.json(EventHeader(mediaType), json)
  }
}

class JsonEventBodyTests extends FunSuite {
  import com.blinkbox.books.messaging.JsonEventBodyTests._

  implicit val json4sJacksonFormats = DefaultFormats

  test("Constructs a JSON event body from a class with the correct content type") {
    val body = JsonEventBody(TestEvent1("wibble"))
    assert(body.contentType.mediaType == TestEvent1.jsonMediaType)
    assert(body.contentType.charset == Some(StandardCharsets.UTF_8))
    assert(body.content != null && body.content.size > 0)
  }

  test("Adds a $schema field with the inferred schema name") {
    val body = JsonEventBody(TestEvent1("wibble"))
    val json = JsonMethods.parse(new ByteArrayInputStream(body.content))
    assert((json \ "$schema") == JString("events.testevent.v1"))
  }

  test("Serializes DateTime objects to ISO format in UTC") {
    val now = new DateTime(2014, 7, 26, 11, 34, 2, 293, DateTimeZone.UTC)
    val body = JsonEventBody(TestEvent2(now))
    val json = JsonMethods.parse(new ByteArrayInputStream(body.content))
    assert((json \ "bar") == JString("2014-07-26T11:34:02.293Z"))
  }

  test("Deconstructs a JSON event body when the media type matches") {
    val event = TestEvent1("wibble")
    val body = JsonEventBody(event)
    assert(JsonEventBody.unapply[TestEvent1](body) == Some(event))
  }

  test("Does not deconstruct a JSON event body when the media type does not match") {
    val event = TestEvent1("wibble")
    val body = JsonEventBody(event)
    assert(JsonEventBody.unapply[TestEvent2](body) == None)
  }

  test("Does not deconstruct a JSON event body which is invalid") {
    val event = jsonEvent()
    assert(JsonEventBody.unapply[TestEvent2](event.body) == None)
  }

  test("Deserializes DateTime objects in UTC") {
    val now = new DateTime(2014, 7, 26, 11, 34, 2, 293, DateTimeZone.UTC)
    val body = JsonEventBody(TestEvent2(now))
    val body2 = JsonEventBody.unapply[TestEvent2](body)
    assert(body2.isDefined && body2.get.bar.getZone == DateTimeZone.UTC)
  }

  test("should support URI serialization/deserialization") {
    val uri = URITestObject(new URI("http://localhost"))
    val eventBody = JsonEventBody(uri)
    val uriDeserialized = JsonEventBody.unapply[URITestObject](eventBody)
    assert(new URI("http://localhost") == uriDeserialized.get.address)
  }
}
