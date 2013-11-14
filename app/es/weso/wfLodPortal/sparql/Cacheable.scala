package es.weso.wfLodPortal.sparql

import play.api.cache.Cache
import play.api.Play.current
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.ResultSetFactory
import com.hp.hpl.jena.query.ResultSetFormatter

trait Cacheable {

  def retrieve(queryStr: String, expiration: Int, method: String => ResultSet): ResultSet = {
    val key = queryStr.hashCode.toString
    val rs = Cache.getOrElse(key, expiration)(ResultSetFormatter.asXMLString(method(queryStr)))
    ResultSetFactory.fromXML(rs)
  }

}