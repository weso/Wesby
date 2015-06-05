package es.weso.wesby.models

import es.weso.wesby.Configurable
import es.weso.wesby.sparql.QueryEngine
import es.weso.wesby.utils.UriFormatter

/**
 * Options is used in order to pass general information
 * to the views.
 * @param partialUri the relative URI (to the baseUri)
 */
class Options(val partialUri: String) extends Configurable {
  import Options._

  val uri = UriFormatter.fullUri(partialUri)

  def query = QueryEngine.applyFilters(fallback, Seq("<" + uri + ">"))

  def host = Options.host
  def endpoint = Options.endpoint
  def fallback = Options.fallback
  def baseUri = Options.baseUri

  def title = Options.title
  def license = Options.license
}

/**
 * Options companion object.
 */
object Options extends Configurable {
  val host = conf.getString("sparql.actualuri")
  val host_r = host.substring(0, host.length() - 1)
  val endpoint = conf.getString("sparql.endpoint")
  val baseUri = conf.getString("sparql.baseuri")
  val fallback = conf.getString("query.show.fallback")

  val title = conf.getString("portal.title")
  val license = conf.getString("portal.license")
}