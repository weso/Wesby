package es.weso.wfLodPortal

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.ResultSetFormatter
import com.hp.hpl.jena.query.Syntax
import models.Node
import models.Property
import models.Resource
import com.hp.hpl.jena.query.QuerySolution
import models.Model
import models.InverseModel

object ModelLoader extends Configurable {

  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val sparqlEndpoint = conf.getString("sparql.endpoint")
  val baseUri = conf.getString("sparql.baseuri")

  def loadUri(uri: String) = {
    val fullUri = "<"+baseUri+uri+">"
   (loadSubject(fullUri),loadPredicate(fullUri))
  }

  def loadSubject(uri: String) = {
    val rs = performQuery {
      applyFilters(querySubject,
        Seq(uri))
    }
    val model = Model(Resource(uri, None))
    while (rs.hasNext()) {
      val qs = rs.next
      model.add(processProperty(qs), processPredicate(qs))
    }
    model
  }

  def loadPredicate(uri: String) = {
    val rs = performQuery {
      applyFilters(queryPredicate,
        Seq(uri))
    }
    val model = InverseModel(Resource(uri, None))
    while (rs.hasNext()) {
      val qs = rs.next
      model.add(processSubject(qs), processProperty(qs))
    }
    model
  }

  protected def processSubject(qs: QuerySolution) = {
    val uri = qs.get("?s").toString
    val sl = qs.get("?sl")
    val label = if (sl == null) { None } else { Some(sl.toString) }
    Resource(uri, label)
  }

  protected def processProperty(qs: QuerySolution) = {
    val uri = qs.get("?v").toString
    val vl = qs.get("?vl")
    val label = if (vl == null) { None } else { Some(vl.toString) }
    Property(uri, label)
  }

  protected def processPredicate(qs: QuerySolution) = {
    val uri = qs.get("?p").toString
    val vl = qs.get("?pl")
    val label = if (vl == null) { None } else { Some(vl.toString) }
    Resource(uri, label)
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
    rs
  }

  private def applyFilters(queryStr: String, args: Seq[String]) = {
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

  def main(args: Array[String]) {
    println(ModelLoader.applyFilters(querySubject, Seq("cex:Value")))
    println(ModelLoader.loadUri("<http://data.webfoundation.org/webindex/v2013/dataset/UN_D-Normalised>"))
  }
}