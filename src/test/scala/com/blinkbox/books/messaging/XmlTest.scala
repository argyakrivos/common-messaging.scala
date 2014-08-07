package com.blinkbox.books.messaging

import javax.xml.transform.stream.StreamSource

import org.joda.time.DateTimeConstants
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import com.blinkbox.books.messaging.Xml.NodeSeqWrapper
import org.xml.sax.SAXParseException

@RunWith(classOf[JUnitRunner])
class XmlTest extends FunSuite {

  val root =
    <root>
      <element>42</element>
      <nested>
        <element>42</element>
      </nested>
      <repeated>foo</repeated>
      <repeated>bar</repeated>
      <date>2013-10-15</date>
      <timestamp>2013-10-15T13:32:51Z</timestamp>
    </root>

  test("Get required value when it exists") {
    assert(root.stringValue("element") == "42")
    assert((root \ "nested").stringValue("element") == "42")
  }

  test("Get required value when it doesn't exist") {
    val ex1 = intercept[IllegalArgumentException] { root.stringValue("nonExistent") }
    assert(ex1.getMessage.startsWith("Expected a single value for path 'nonExistent' on node <root>"))

    val ex2 = intercept[IllegalArgumentException] { (root \ "nested").stringValue("nonExistent") }
    assert(ex2.getMessage.startsWith("Expected a single value for path 'nonExistent' on node <nested>"))
  }

  test("Get required value when there are more than one element with the same name") {
    val ex = intercept[IllegalArgumentException] { root.stringValue("repeated") }
    assert(ex.getMessage.startsWith("Expected a single value for path 'repeated'"))
  }

  test("Get optional value when it exists") {
    assert(root.stringValueOptional("element") == Some("42"))
    assert((root \ "nested").stringValueOptional("element") == Some("42"))
  }

  test("Get optional value when it doesn't exist") {
    assert(root.stringValueOptional("nonExistent") == None)
  }

  test("Get optional Int value when it exists") {
    assert(root.intValueOptional("element") == Some(42))
  }

  test("Get optional Int value when it does not exist") {
    assert(root.intValueOptional("nonExistent") == None)
  }

  test("Get optional Int value when it is invalid") {
    intercept[NumberFormatException] { root.intValueOptional("date") }
  }

  test("Get Joda DateTime from a timestamp") {
    val t = root.dateTimeValue("timestamp")
    assert(t.getYear == 2013)
    assert(t.getMonthOfYear == DateTimeConstants.OCTOBER)
    assert(t.getDayOfMonth == 15)
    assert(t.getHourOfDay == 13)
    assert(t.getMinuteOfHour == 32)
    assert(t.getSecondOfMinute == 51)
    assert(t.getMillisOfSecond == 0)
  }

  test("Get Joda DateTime from invalid timestamp fields") {
    intercept[IllegalArgumentException] { <r><ts>foo</ts></r>.dateTimeValue("ts") }
    intercept[IllegalArgumentException] { <r><ts>2013-10-15</ts></r>.dateTimeValue("ts") }
    intercept[IllegalArgumentException] { <r><ts>2013-10-15</ts></r>.dateTimeValue("ts") }
    intercept[IllegalArgumentException] { <r><ts>2013-10-15 13:32:51Z</ts></r>.dateTimeValue("ts") }
  }

  test("Get Joda DateTime from date field without time") {
    val t = root.dateValue("date")
    assert(t.getYear == 2013)
    assert(t.getMonthOfYear == DateTimeConstants.OCTOBER)
    assert(t.getDayOfMonth == 15)
    assert(t.getHourOfDay == 0)
    assert(t.getMinuteOfDay == 0)
    assert(t.getSecondOfDay == 0)
  }

  test("Get Joda DateTime from invalid date field without time") {
    intercept[IllegalArgumentException] { <r><date>foo</date></r>.dateValue("date") }
    intercept[IllegalArgumentException] { <r><date>13:32:51</date></r>.dateValue("date") }
    intercept[IllegalArgumentException] { <r><date>T13:32:51</date></r>.dateValue("date") }
    intercept[IllegalArgumentException] { <r><date>2013-10-15T13:32:51Z</date></r>.dateValue("date") }
  }

  test("Schema validator throws an exception when schema path is invalid") {
    intercept[IllegalArgumentException] {
      Xml.validatorFor("i-do-not-exist.xsd")
    }
  }

  test("Validate valid message with schema") {
    val validator = Xml.validatorFor("/versioning.xsd", "/test.xsd")
    validator.validate(new StreamSource(getClass.getResourceAsStream("/validMsg.xml")))
  }

  test("Validate invalid message with schema") {
    val validator = Xml.validatorFor("/versioning.xsd", "/test.xsd")
    val ex = intercept[SAXParseException] {
      validator.validate(new StreamSource(getClass.getResourceAsStream("/invalidMsg.xml")))
    }
    assert(ex.getMessage.startsWith("cvc-complex-type.2.4.a: Invalid content was found starting with element 'name'"))
  }
}
