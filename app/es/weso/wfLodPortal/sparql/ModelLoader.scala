package es.weso.wfLodPortal.sparql

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.Syntax
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.InverseModel
import es.weso.wfLodPortal.models.LazyDataStore
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfAnon
import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfNode
import es.weso.wfLodPortal.models.RdfProperty
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.ResultQuery
import es.weso.wfLodPortal.utils.UriFormatter
import es.weso.wfLodPortal.utils.UriFormatter._
import es.weso.wfLodPortal.models.Uri
import play.Logger

object ModelLoader extends Configurable {

  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val baseUri = conf.getString("sparql.baseuri")
  val actualUri = conf.getString("sparql.actualuri")
  val indexPath = conf.getString("sparql.index")

  val Subject = "?s"
  val Verb = "?v"
  val Predicate = "?p"
    
  def fullUri(uri: String) = {
    baseUri + uri
  }

  def loadUri(uri: String) = {
    val fullUri: Uri = UriFormatter.format(baseUri + uri)
    val subject = LazyDataStore(fullUri, loadSubject)
    val predicate = LazyDataStore(fullUri, loadPredicate)
    ResultQuery(subject, predicate)
  }

  def loadUri(sufix: String, preffix: String) = {
    val fullUri = UriFormatter.format(uriToBaseURI(sufix + preffix))
    Logger.info(fullUri.toString)
    val subject = LazyDataStore(fullUri, loadSubject)
    val predicate = LazyDataStore(fullUri, loadPredicate)
    ResultQuery(subject, predicate)
  }

  protected def loadSubject(uri: String): Model = {
    val rs = QueryEngine.performQuery(querySubject, Seq("<" + uri + ">"))
    val jenaModel = ModelFactory.createDefaultModel
    val model = Model(jenaModel)
    val resource = ResourceFactory.createResource(uri)
    while (rs.hasNext) {
      val qs = rs.next
      val property = processProperty(qs, Verb)
      val predicate = processPredicate(qs)
      jenaModel.add(resource, property.property, predicate.rdfNode)
      model.add(property, predicate)
    }
    model
  }

  def loadPredicate(uri: String): InverseModel = {
    val rs = QueryEngine.performQuery(queryPredicate, Seq("<" + uri + ">"))

    val jenaModel = ModelFactory.createDefaultModel
    val model = InverseModel(jenaModel)
    val resource = ResourceFactory.createResource(uri)

    while (rs.hasNext) {
      val qs = rs.next
      val vl = qs.get(Subject)
      val property = processProperty(qs, Verb)
      if (vl.isAnon()) {
        val subject = RdfAnon(vl.asResource())
        jenaModel.add(subject.resource, property.property, resource)
        model.add(subject, property)
      } else {
        val subject = processResource(qs, Subject)
        jenaModel.add(subject.resource, property.property, resource)
        model.add(subject, property)
      }
    }
    model
  }

  protected def processPredicate(qs: QuerySolution): RdfNode = {
    val uri = qs.get(Predicate)
    uri match {
      case a if a.isAnon =>
        RdfAnon(a.asResource)
      case e if e.isLiteral =>
        val literal = e.asLiteral
        val dataType = literal.getDatatype match {
          case dt if dt != null =>
            Some(UriFormatter.format(dt.getURI))
          case _ => None
        }
        RdfLiteral(literal.getValue.toString, dataType, uri)
      case e if e.isResource => processResource(qs, Predicate)
    }
  }

  protected def processResource(qs: QuerySolution, param: String) = {
    val vl = qs.get(param)
    val uri = UriFormatter.format(vl.asResource.getURI)
    val dss = processResultQuery(uri)
    RdfResource(uri, dss, vl.asResource)
  }

  protected def processProperty(qs: QuerySolution, param: String): RdfProperty = {
    val uri = UriFormatter.format(qs.get(param).asResource.getURI)
    val dss = processResultQuery(uri)
    val property = ResourceFactory.createProperty(uri.absolute)
    RdfProperty(uri, dss, property)
  }

  protected def processResultQuery(uri: Uri) = {
    val subject = Some(LazyDataStore(uri, loadSubject))
    val predicate = Some(LazyDataStore(uri, loadPredicate))
    ResultQuery(subject, predicate)
  }

}