package com.blinkbox.books.messaging

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.{ DateFormat, SimpleDateFormat }
import java.util.Date
import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.Source
import javax.xml.XMLConstants
import scala.xml.NodeSeq

/**
 * Collection of utility functions for parsing XML messages from platform services.
 */
object Xml {

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
    def value(path: String): String = {
      val found = nodeSeq \ path
      if (found.size != 1) throw new IllegalArgumentException(s"Expected a single value for path '$path' on node $nodeSeq, got: $found")
      else found.text.trim
    }

    /**
     * Get the value of the a direct child with the given path if it exists, or return None if no such value exists.
     *
     *  Throws an exception if multiple matching values exist.
     */
    def optionalValue(path: String): Option[String] = {
      val found = nodeSeq \ path
      if (found.size > 1) throw new IllegalArgumentException(s"Expected at most one value for path '$path' on node $nodeSeq, got: $found")
      else if (found.size == 1) Some(found.text.trim)
      else None
    }

  }

}
