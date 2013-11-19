package es.weso.wfLodPortal.sparql.custom

import es.weso.wfLodPortal.Configurable
import play.api.Logger

trait CustomQuery extends Configurable {

  val baseUri = conf.getString("sparql.baseuri")

  def checkMode(param: String, version: String) = {
    new StringBuilder(baseUri)
      .append(param).append("/v")
      .append(version).append("/").toString
  }
}