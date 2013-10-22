package es.weso.wfLodPortal

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSetFormatter
import com.hp.hpl.jena.query.Syntax

object ModelLoader extends Configurable {
  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val sparqlEndpoint = conf.getString("sparql.endpoint")

  def loadUri(uri: String) = {

  }

  protected def performQuery(queryStr: String) = {
    val query = QueryFactory.create(queryStr, Syntax.syntaxARQ)
    val qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)
    val rs = qexec.execSelect()
    if (rs == null) {
      throw new Exception("Invalid ResultSet")
    }
    if (!rs.hasNext()) {
      throw new IllegalArgumentException(
        "ResultSet was empty. Check your query:\n" + query);
    }
    ResultSetFormatter.asXMLString(rs)
  }

  def applyFilters(queryName: String, args: Seq[String]) = {
    
    val query = new StringBuilder(queryName)
    for (arg <- args.zipWithIndex) {
      val holder = new StringBuilder("{").append(arg._2).append("}")
      replace(query, holder.toString, arg._1)
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
  
  def main(args: Array[String]) {
    println(ModelLoader.applyFilters(querySubject, Seq("cex:Value")))
  }
}