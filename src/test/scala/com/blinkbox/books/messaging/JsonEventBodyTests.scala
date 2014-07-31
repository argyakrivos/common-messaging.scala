package com.blinkbox.books.messaging

import java.nio.charset.StandardCharsets

import org.scalatest.FunSuite

object JsonEventBodyTests {
  case class TestEvent1(foo: String)
  case class TestEvent2(foo: String, bar: Int)

  implicit object TestEvent1 extends JsonEventBody[TestEvent1] {
    val jsonMediaType = MediaType("application/x-testevent1+json")
  }

  implicit object TestEvent2 extends JsonEventBody[TestEvent2] {
    val jsonMediaType = MediaType("application/x-testevent2+json")
  }
}

class JsonEventBodyTests extends FunSuite {
  import JsonEventBodyTests._

  test("Constructs a JSON event body from a class with the correct content type") {
    val body = JsonEventBody(TestEvent1("wibble"))
    assert(body.contentType.mediaType == TestEvent1.jsonMediaType)
    assert(body.contentType.charset == Some(StandardCharsets.UTF_8))
    assert(body.content != null && body.content.size > 0)
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
}
