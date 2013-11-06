package es.weso.wfLodPortal.sparql

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.Syntax

import es.weso.wfLodPortal.Configurable

object QueryEngine extends Cacheable with Configurable {

  val sparqlEndpoint = conf.getString("sparql.endpoint")
  val cacheExpiration = conf.getInt("sparql.expiration")

  def performQuery(queryStr: String): ResultSet = {
    retrieve(queryStr, cacheExpiration, executeQuery)
  }

  def performQuery(queryStr: String, args: Seq[String]): ResultSet = {
    val query = applyFilters(queryStr, args)
    retrieve(query, cacheExpiration, executeQuery)
  }

  protected def executeQuery(queryStr: String): ResultSet = {
    val query = QueryFactory.create(queryStr, Syntax.syntaxARQ)
    val qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)
    val rs = qexec.execSelect()
    if (rs == null) {
      throw new Exception("Invalid ResultSet")
    }
    rs
  }

  private def applyFilters(queryStr: String, args: Seq[String]): String = {
    val query = new StringBuilder(queryStr)
    for (arg <- args) {
      replace(query, "{0}", arg)
    }
    query.toString

  }

  private def replace(builder: StringBuilder, target: String, replacement: String): StringBuilder = {
    var indexOfTarget = builder.indexOf(target)
    while (indexOfTarget >= 0) {
      builder.replace(indexOfTarget, indexOfTarget + target.length, replacement)
      indexOfTarget = builder.indexOf(target)
    }
    builder
  }
}