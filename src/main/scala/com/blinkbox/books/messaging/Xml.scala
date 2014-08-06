package com.blinkbox.books.messaging

import java.io.InputStream
import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.Source
import javax.xml.XMLConstants

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import scala.xml.NodeSeq

/**
 * Collection of utility functions for parsing XML messages from platform services.
 */
object Xml {

  private def schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

  /**
   * The resource for the schema will be looked up on the classpath. The directory
   * of the main schema will be used as the base directory for the validator, which means
   * that imported schemas in the same directory will be automatically picked up.
   *
   * @param schemaNames A list of schema names to use. Note, that the order of the names matters.
   * @return A validator for a combined schema
   */
  def validatorFor(schemaNames: String*) = {
    var inputs = Array[InputStream]()
    try {
      for (schemaName <- schemaNames) {
        inputs = inputs :+ getClass.getResourceAsStream(schemaName)
      }
      val sources: Array[Source] = inputs.map(new StreamSource(_))
      schemaFactory.newSchema(sources).newValidator()
    } finally {
      inputs.foreach(_.close())
    }
  }

  case class XMLParsingException(message: String, cause: Throwable = null) extends Exception(message, cause)

  /**
   * Wrapper class that adds convenience methods for getting values to the NodeSeq
   * objects returned by path operations on XML. These allow getting of values while also
   * validating that expected values are there (e.g. single values), and giving useful
   * error messages when the expectation isn't met.
   */
  implicit class NodeSeqWrapper(nodeSeq: NodeSeq) {

    /**
     * Get the string value of the one and only direct child with the given path, otherwise throw an exception.
     */
    def stringValue(path: String): String = {
      val found = nodeSeq \ path
      if (found.size == 0) throw XMLParsingException(s"No matching path '$path' found on node '$nodeSeq'")
      if (found.size > 1) throw XMLParsingException(
        s"Expected a single value for path '$path' on node '$nodeSeq', got ${found.size}")
      found.text.trim
    }

    /**
     * Get the string value of a direct child with the given path if it exists, or return None if no such value exists.
     *
     *  Throws an exception if multiple matching values exist.
     */
    def stringValueOptional(path: String): Option[String] = {
      val found = nodeSeq \ path
      if (found.size > 1) throw XMLParsingException(s"Expected at most one value for path '$path' on node '$nodeSeq', got: ${found.size}")
      else if (found.size == 1) Some(found.text.trim)
      else None
    }

    /**
     * Get the Int value of a direct child with the given path if it exists, or return None otherwise.
     */
    def intValueOptional(path: String): Option[Int] =
      try {
        stringValueOptional(path).map(_.toInt)
      } catch {
        case ex: Throwable => throw XMLParsingException("Cannot parse integer", ex)
      }

    /**
     * Get the Joda DateTime value of the one and only direct child with the given path, otherwise throw an exception.
     *
     * The result is in ISO8601 format without millis and converted to UTC
     */
    def dateTimeValue(path: String): DateTime = {
      val value = stringValue(path)
      try {
        ISODateTimeFormat.dateTimeNoMillis.withZoneUTC.parseDateTime(value)
      } catch {
        case ex: Throwable => throw XMLParsingException(s"Could not parse date from '$value'", ex)
      }
    }

    /**
     * Get the Joda DateTime value of the one and only direct child with the given path, otherwise throw an exception.
     * Input string is expected to be formatted as "yyyy-MM-dd".
     *
     * The result is in ISO8601 format and converted to UTC
     */
    def dateValue(path: String): DateTime = {
      val value = stringValue(path)
      try {
        DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC.parseDateTime(value)
      } catch {
        case ex: Throwable => throw XMLParsingException(s"Could not parse date from '$value'", ex)
      }
    }
  }
}
