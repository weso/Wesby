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
import models.ResultQuery
import com.hp.hpl.jena.rdf.model.ModelFactory
import models.Literal
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction
import com.hp.hpl.jena.rdf.model.ResourceFactory
import models.LazyDataStore

object ModelLoader extends Configurable {

  val replaceable = true
  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val sparqlEndpoint = conf.getString("sparql.endpoint")
  val baseUri = conf.getString("sparql.baseuri")
  val actualUri = conf.getString("sparql.actualuri")
  val indexPath = conf.getString("sparql.index")

  def loadUri(uri: String) = {
    val fullUri = baseUri + uri
    val subject = loadSubject(fullUri)
    val predicate = loadPredicate(fullUri)
    ResultQuery(subject, predicate)
  }

  def loadSubject(uri: String): Model = {
    println("Entra")
    val rs = performQuery {
      applyFilters(querySubject,
        Seq("<" + uri + ">"))
    }
    val jenaModel = ModelFactory.createDefaultModel
    val model = Model(jenaModel)
    val resource = ResourceFactory.createResource(uri)
    while (rs.hasNext) {
      val qs = rs.next
      val property = processProperty(qs)
      val predicate = processPredicate(qs)
      jenaModel.add(resource, property.property, predicate.rdfNode)
      predicate match {
        case r: Resource =>
          model.add(property, r, LazyDataStore(r.uri, loadSubject))
        case l: Literal => model.add(property, l)
        case _ => {}
      }
    }
    model
  }

  def loadPredicate(uri: String): InverseModel = {
    val rs = performQuery {
      applyFilters(queryPredicate,
        Seq("<" + uri + ">"))
    }
    val jenaModel = ModelFactory.createDefaultModel
    val model = InverseModel(jenaModel)
    val resource = ResourceFactory.createResource(uri)
    while (rs.hasNext) {
      val qs = rs.next
      val subject = processSubject(qs)
      val property = processProperty(qs)
      jenaModel.add(subject.resource, property.property, resource)
      model.add(subject, property, LazyDataStore(subject.uri, loadPredicate))
    }
    model
  }

  protected def processSubject(qs: QuerySolution): Resource = {
    val uri = qs.get("?s")
    val sl = qs.get("?sl")
    val label = if (sl == null) { None } else { Some(sl.toString) }

    Resource(UriFormatter.format(uri.toString), label, uri.asResource)
  }

  protected def processProperty(qs: QuerySolution): Property = {
    val uri = qs.get("?v").toString
    val vl = qs.get("?vl")
    val label = if (vl == null) { None } else { Some(vl.toString) }
    Property(UriFormatter.format(uri), label, ResourceFactory.createProperty(uri))
  }

  protected def processPredicate(qs: QuerySolution): Node = {

    val uri = qs.get("?p")

    uri match {
      case e if e.isLiteral() =>
        val literal = e.asLiteral
        val dataType = literal.getDatatype match {
          case dt if dt != null =>
            Some(UriFormatter.format(dt.getURI))
          case _ => None
        }
        Literal(literal.getValue.toString, dataType, uri)
      case e if e.isResource() =>
        val vl = qs.get("?pl")
        val label = if (vl == null) { None } else { Some(vl.toString) }
        Resource(UriFormatter.format(uri.toString), label, uri.asResource)
    }
  }

  protected def performQuery(queryStr: String) = {
    val query = QueryFactory.create(queryStr, Syntax.syntaxARQ)
    val qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)
    val rs = qexec.execSelect()
    if (rs == null) {
      throw new Exception("Invalid ResultSet")
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
  }
}