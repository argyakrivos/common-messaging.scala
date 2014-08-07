package com.blinkbox.books.messaging

import java.io.InputStream
import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.Source
import javax.xml.XMLConstants

import org.joda.time.{LocalDate, DateTime}
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import scala.xml.NodeSeq

/**
 * Collection of utility functions for parsing XML messages from platform services.
 */
object Xml {

  // The SchemaFactory class is not thread-safe, hence this is a def
  private def schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

  /**
   * The resource for the schema will be looked up on the classpath. The directory
   * of the main schema will be used as the base directory for the validator, which means
   * that imported schemas in the same directory will be automatically picked up.
   *
   * @param schemaNames A list of schema names to use. Note, that the order of the names matters.
   * @return A validator for a combined schema
   */
  def validatorFor(schemaPaths: String*) = {
    var inputs = Array[InputStream]()
    try {
      for (schemaPath <- schemaPaths) {
        val input = getClass.getResourceAsStream(schemaPath)
        if (input == null) throw new IllegalArgumentException(s"Cannot find schema with path '$schemaPath'")
        else inputs = inputs :+ input
      }
      val sources: Array[Source] = inputs.map(new StreamSource(_))
      schemaFactory.newSchema(sources).newValidator()
    } finally {
      inputs.foreach(_.close())
    }
  }

  /**
   * Wrapper class that adds convenience methods for getting values to the NodeSeq
   * objects returned by path operations on XML. These allow getting of values while also
   * validating that expected values are there (e.g. single values), and giving useful
   * error messages when the expectation isn't met.
   */
  implicit class NodeSeqWrapper(nodeSeq: NodeSeq) {

    /**
     * Get the value of the one and only direct child with the given path, otherwise throw an exception.
     */
    @deprecated("Use stringValue() instead")
    def value(path: String): String = stringValue(path)

    /**
     * Get the string value of the one and only direct child with the given path, otherwise throw an exception.
     */
    def stringValue(path: String): String = {
      val found = nodeSeq \ path
      if (found.size != 1) throw new IllegalArgumentException(s"Expected a single value for path '$path' on node $nodeSeq, got: ${found.size}")
      else found.text.trim
    }

    /**
     * Get the value of the a direct child with the given path if it exists, or return None if no such value exists.
     *
     *  Throws an exception if multiple matching values exist.
     */
    @deprecated("Use stringValueOptional() instead")
    def optionalValue(path: String): Option[String] = stringValueOptional(path)

    /**
     * Get the string value of a direct child with the given path if it exists, or return None if no such value exists.
     *
     *  Throws an exception if multiple matching values exist.
     */
    def stringValueOptional(path: String): Option[String] = {
      val found = nodeSeq \ path
      if (found.size > 1) throw new IllegalArgumentException(s"Expected at most one value for path '$path' on node '$nodeSeq', got: ${found.size}")
      else if (found.size == 1) Some(found.text.trim)
      else None
    }

    /**
     * Get the Int value of a direct child with the given path if it exists, or return None otherwise.
     */
    def intValueOptional(path: String): Option[Int] = stringValueOptional(path).map(_.toInt)

    /**
     * Get the Joda DateTime value from a date in ISO8601 format without millis. The result is converted to UTC.
     */
    def dateTimeValue(path: String): DateTime =
      ISODateTimeFormat.dateTimeNoMillis.withZoneUTC.parseDateTime(stringValue(path))

    /**
     * Get the Joda LocalDate value of the one and only direct child with the given path, otherwise throw an exception.
     * Input string is expected to be formatted as "yyyy-MM-dd".
     *
     * The result is converted to UTC
     */
    def dateValue(path: String): LocalDate =
      DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC.parseLocalDate(stringValue(path))
  }
}
