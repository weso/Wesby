package es.weso.wesby.sparql

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.Syntax

import es.weso.wesby.Configurable

/**
 * Executes the queries and handles the partial (parametrized) queries.
 */
object QueryEngine extends Cacheable with Configurable {

  val sparqlEndpoint = conf.getString("sparql.endpoint")
  val cacheExpiration = conf.getInt("sparql.expiration")

  /**
   * Queries to the triple-store retrieving a Jena's ResultSet.
   * @param queryStr the query to be executed.
   */
  def performQuery(queryStr: String): ResultSet = {
    retrieve(queryStr, cacheExpiration, executeQuery)
  }

  /**
   * Queries to the triple-store retrieving a Jena's ResultSet.
   * @param queryStr the partial query
   * @param args the arguments to be included into the query
   */
  def performQuery(queryStr: String, args: Seq[String]): ResultSet = {
    val query = applyFilters(queryStr, args)
    retrieve(query, cacheExpiration, executeQuery)
  }

  /**
   * Executes the query against the triple-store retrieving a Jena's ResultSet.
   * @param queryStr the query to be executed.
   */
  protected def executeQuery(queryStr: String): ResultSet = {
    val query = QueryFactory.create(queryStr, Syntax.syntaxARQ)
    val qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)
    val rs = qexec.execSelect()
    if (rs == null) {
      throw new Exception("Invalid ResultSet")
    }
    rs
  }

  /**
   * Includes the Seq of arguments into the partial query.
   * @param queryStr the partial query
   * @param args the Seq of arguments to be included into the partial query.
   */
  def applyFilters(queryStr: String, args: Seq[String]): String = {
    val query = new StringBuilder(queryStr)
    for (arg <- args.zipWithIndex) {
      val replacement = new StringBuilder("{").append(arg._2).append("}")
      replace(query, replacement.toString, arg._1)
    }
    query.toString

  }

  /**
   * Replaces the replacement into the target
   * @param builder the objective text
   * @param target the text to be replaced
   * @param replacement the replacement text
   */
  private def replace(builder: StringBuilder, target: String, replacement: String): StringBuilder = {
    var indexOfTarget = builder.indexOf(target)
    while (indexOfTarget >= 0) {
      builder.replace(indexOfTarget, indexOfTarget + target.length, replacement)
      indexOfTarget = builder.indexOf(target)
    }
    builder
  }
}