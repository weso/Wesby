package models.http

import play.api.http.{HeaderNames, ContentTypes, MimeTypes}

/**
 * Defines custom HTTP Content-Type header values, according to the current available Codec.
 */
object CustomContentTypes extends CustomContentTypes

trait CustomContentTypes extends ContentTypes {
  
  import play.api.mvc.Codec

  /**
   * Content-Type of turtle.
   */
  def TURTLE(implicit codec: Codec) = withCharset(CustomMimeTypes.TURTLE)

  /**
   * Content-Type of n-triples.
   */
  def NTRIPLES(implicit codec: Codec) = withCharset(CustomMimeTypes.NTRIPLES)

  /**
   * Content-Type of json-ld.
   */
  def JSONLD(implicit codec: Codec) = withCharset(CustomMimeTypes.JSONLD)

  /**
   * Content-Type of n3.
   */
  def N3(implicit codec: Codec) = withCharset(CustomMimeTypes.N3)

  /**
   * Content-Type of rdf-xml.
   */
  def RDFXML(implicit codec: Codec) = withCharset(CustomMimeTypes.RDFXML)
}

/** Common HTTP MIME types */
object CustomMimeTypes extends CustomMimeTypes

trait CustomMimeTypes extends MimeTypes {

  /**
   * Content-Type of turtle.
   */
  val TURTLE = "text/turtle"

  /**
   * Content-Type of n-triples.
   */
  val NTRIPLES = "application/n-triples"

  /**
   * Content-Type of json-ld.
   */
  val JSONLD = "application/ld+json"

  /**
   * Content-Type of n3.
   */
  val N3 = "text/n3"

  /**
   * Content-Type of rdf-xml.
   */
  val RDFXML = "application/rdf+xml"

}

/** Defines standard LDP HTTP headers. */
object CustomHeaderNames extends HeaderNames

/** Defines standard LDP HTTP headers. */
trait CustomHeaderNames {

  val LINK = "Link"

}
