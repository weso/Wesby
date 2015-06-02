package views.helpers

import es.weso.wesby.Configurable
import es.weso.wesby.models.{RdfProperty, RdfResource, ResultQuery}
import es.weso.wesby.sparql.Handlers.{handleFirstLiteralAsValue, handleResourceAsString}
import es.weso.wesby.utils.CommonURIS.{rdf, rdfs}
import play.api.Play.current
import play.api.cache.Cache
import play.api.templates.Html

/**
 * Includes template-oriented helpers and utility methods.
 */
object Utils extends Configurable {

  val Empty = ""

  val cacheExpiration = conf.getInt("sparql.expiration")

  def toUpper(text: String) = text.toUpperCase

  def toUpper(html: Html) = html.toString.toUpperCase

  def toLower(text: String) = text.toLowerCase

  def toLower(html: Html) = html.toString.toLowerCase

  /**
   * Retrieves a label from the cache, if the label is not within the
   * cache, it returns it, and stores it back in the cache.
   * @param r the target RdfProperty
   */
  def cachedLabel(r: RdfProperty): String = {
    val key = r.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(r.dss))
  }

  /**
   * Retrieves a label from the cache, if the label is not within the
   * cache, it returns it, and stores it back in the cache.
   * @param r the target RdfResource
   */
  def cachedLabel(r: RdfResource): String = {
    val key = r.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(r.dss))
  }

  /**
   * Retrieves a label from the cache, if the label is not within the
   * cache, it returns it, and stores it back in the cache.
   * @param r the target ResultQuery
   */
  def cachedLabel(rs: ResultQuery): String = {
    val key = rs.pred.get.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(rs))
  }

  /**
   * Returns the rdf:type from a ResultQuery.
   * @param resultQuery the target ResultQuery
   */
  def rdfType(resultQuery: ResultQuery): String = {
    val result = handleResourceAsString(resultQuery.subject.get,
      rdf, "type", (r: RdfResource) => { r.uri.relative })
    if (result.isEmpty)
      "Unknown rdf:type"
    else result
  }

  /**
   * Returns the label of its rdf:type from a ResultQuery.
   * @param resultQuery the target ResultQuery
   */
  def rdfTypeLabel(resultQuery: ResultQuery): String = {
    handleResourceAsString(resultQuery.subject.get,
      rdf, "type",
      (r: RdfResource) => cachedLabel(r))
  }

  /**
   * Retrieves the rdfs:label from a given ResultQuery.
   * @param resultQuery the target ResultQuery
   */
  def label(resultQuery: ResultQuery): String = {
    handleFirstLiteralAsValue(resultQuery.subject.get,
      rdfs, "label")
  }

  /**
   * Displays a label, if the label is empty or is not in English, it displays
   * the URI.
   * @param uri the URI of the RDF resource
   * @param label the label to be display
   */
  def showLabel(uri: String, label: String): String = {
    val chunks = label.split("@")
    if (chunks(0).isEmpty)
      uri
    else if (chunks.length > 1 && chunks(chunks.length - 1) == "en")
      chunks(0)
    else label
  }

}