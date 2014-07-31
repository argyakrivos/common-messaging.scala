package com.blinkbox.books.messaging

import org.scalatest.FunSuite

class MediaTypeTests extends FunSuite {

  test("Can be constructed with a main type and subtype") {
    val mediaType = MediaType("application", "xml")
    assert(mediaType.mainType == "application")
    assert(mediaType.subType == "xml")
  }

  test("Can be constructed by parsing a full media type") {
    val mediaType = MediaType("application/xml")
    assert(mediaType.mainType == "application")
    assert(mediaType.subType == "xml")
  }

  test("Does not support parsing media types with parameters") {
    // because for simplicity's sake we don't support parameters in the implementation, this test just makes
    // sure that if anybody doesn't realise this and tries to use the parsing function to make one with
    // parameters that their code will blow up rather than either omitting them or having a wrong subtype
    intercept[IllegalArgumentException] {
      MediaType("application/xml; foo=bar")
    }
  }

  test("Produces a valid media type in toString") {
    val mediaType = MediaType("application", "xml")
    assert(mediaType.toString == "application/xml")
  }

}
