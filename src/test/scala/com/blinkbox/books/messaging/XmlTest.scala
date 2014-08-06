package com.blinkbox.books.messaging

import org.joda.time.DateTimeConstants
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import com.blinkbox.books.messaging.Xml.{XMLParsingException, NodeSeqWrapper}

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
    val ex1 = intercept[XMLParsingException] { root.stringValue("nonExistent") }
    assert(ex1.getMessage.startsWith("No matching path 'nonExistent' found"))

    val ex2 = intercept[XMLParsingException] { (root \ "nested").stringValue("nonExistent") }
    assert(ex2.getMessage.startsWith("No matching path 'nonExistent' found"))
  }

  test("Get required value when there are more than one element with the same name") {
    val ex = intercept[XMLParsingException] { root.stringValue("repeated") }
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
    val ex = intercept[XMLParsingException] { root.intValueOptional("date") }
    assert(ex.cause.isInstanceOf[NumberFormatException])
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
    intercept[XMLParsingException] { <r><ts>foo</ts></r>.dateTimeValue("ts") }
    intercept[XMLParsingException] { <r><ts>2013-10-15</ts></r>.dateTimeValue("ts") }
    intercept[XMLParsingException] { <r><ts>2013-10-15</ts></r>.dateTimeValue("ts") }
    intercept[XMLParsingException] { <r><ts>2013-10-15 13:32:51Z</ts></r>.dateTimeValue("ts") }
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

  test("Get Jda DateTime from invalid date field without time") {
    intercept[XMLParsingException] { <r><date>foo</date></r>.dateValue("date") }
    intercept[XMLParsingException] { <r><date>13:32:51</date></r>.dateValue("date") }
    intercept[XMLParsingException] { <r><date>T13:32:51</date></r>.dateValue("date") }
    intercept[XMLParsingException] { <r><date>2013-10-15T13:32:51Z</date></r>.dateValue("date") }
  }
}
