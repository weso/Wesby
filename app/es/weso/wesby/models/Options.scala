package es.weso.wesby.models

import es.weso.wesby.Configurable
import es.weso.wesby.sparql.QueryEngine
import es.weso.wesby.utils.UriFormatter

class Options(val partialUri: String) extends Configurable {
  import Options._

  val uri = UriFormatter.fullUri(partialUri)

  val mode = if (uri contains "odb/") "odb" else "webindex"

  def query = QueryEngine.applyFilters(fallback, Seq("<" + uri + ">"))
  
  def host = Options.host
  def endpoint = Options.endpoint
  def fallback = Options.fallback
  def baseUri = Options.baseUri
  
  def title = Options.title
  def license = Options.license
}

object Options extends Configurable {
  val host = conf.getString("sparql.actualuri")
  val endpoint = conf.getString("sparql.endpoint")
  val baseUri = conf.getString("sparql.baseuri")
  val fallback = conf.getString("query.show.fallback")
  
  val title = conf.getString("portal.title")
  val license = conf.getString("portal.license")
}