package es.weso.wfLodPortal.models

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.sparql.QueryEngine
import es.weso.wfLodPortal.utils.UriFormatter

class Options(val partialUri: String) extends Configurable {
  import Options._

  val uri = UriFormatter.fullUri(partialUri)

  val mode = if (uri contains "odb/") "odb" else "webindex"

  def query = QueryEngine.applyFilters(fallback, Seq("<" + uri + ">"))

  def version = defaultVersion

  def host = Options.host
  def endpoint = Options.endpoint
  def defaultVersion = Options.defaultVersion
  def fallback = Options.fallback
  def baseUri = Options.baseUri
}

object Options extends Configurable {
  val host = conf.getString("sparql.actualuri")
  val endpoint = conf.getString("sparql.endpoint")
  val defaultVersion = conf.getString("application.version")
  val baseUri = conf.getString("sparql.baseuri")
  val fallback = conf.getString("query.show.fallback")
}