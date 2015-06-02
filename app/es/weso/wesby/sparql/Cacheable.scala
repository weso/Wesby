package es.weso.wesby.sparql

import com.github.mumoshu.play2.memcached.MemcachedPlugin
import com.hp.hpl.jena.query.{ResultSet, ResultSetFactory, ResultSetFormatter}
import com.hp.hpl.jena.sparql.resultset.ResultSetException
import play.api.Play.current
import play.api.cache.Cache

/**
 * Adds the cacheable behavior to QueryEngine
 */
trait Cacheable {

  protected val api = play.api.Play.current.plugin[MemcachedPlugin].get.api

  /**
   * Returns the result of the query from the cache, if there is not cached or 
   * the cache has expired, it performs the query and save it back to the cache.
   * @param queryStr the query to be executed
   * @param expiration the expiration time in seconds
   * @param method Callback to be applied if the query is no cached or the 
   * cache expired
   */
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