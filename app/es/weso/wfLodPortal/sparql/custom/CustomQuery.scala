package es.weso.wfLodPortal.sparql.custom

import es.weso.wfLodPortal.Configurable

trait CustomQuery extends Configurable{
  def checkMode(param: String) = param match {
    case "webindex" => "http://data.webfoundation.org/webindex/v2013/"
    case "odb" => "http://data.webfoundation.org/odb/v2013/"
  }
}