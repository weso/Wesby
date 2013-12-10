package es.weso.wesby.sparql

import com.github.mumoshu.play2.memcached.MemcachedPlugin
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.ResultSetFactory
import com.hp.hpl.jena.query.ResultSetFormatter
import com.hp.hpl.jena.sparql.resultset.ResultSetException

import play.api.Play.current
import play.api.cache.Cache

trait Cacheable {

  protected val api = play.api.Play.current.plugin[MemcachedPlugin].get.api

  def retrieve(queryStr: String, expiration: Int, method: String => ResultSet): ResultSet = {
    val key = queryStr.hashCode.toString
    val rs = Cache.getOrElse(key, expiration)(ResultSetFormatter.asXMLString(method(queryStr)))
    try {
      ResultSetFactory.fromXML(rs)
    } catch {
      case e: ResultSetException =>
        api.remove(key)
        throw e
    }
  }

}