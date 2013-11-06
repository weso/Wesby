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
import es.weso.wfLodPortal.models.OptionalResultQuery
import es.weso.wfLodPortal.models.RdfAnon
import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfNode
import es.weso.wfLodPortal.models.RdfProperty
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.ResultQuery
import es.weso.wfLodPortal.utils.UriFormatter

object ModelLoader extends Configurable {

  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val baseUri = conf.getString("sparql.baseuri")
  val actualUri = conf.getString("sparql.actualuri")
  val indexPath = conf.getString("sparql.index")

  def loadUri(uri: String) = {
    val fullUri = baseUri + uri
    val subject = loadSubject(fullUri)
    val predicate = loadPredicate(fullUri)
    ResultQuery(subject, predicate)
  }

  protected def loadSubject(uri: String): Model = {
    val rs = QueryEngine.performQuery(querySubject, Seq("<" + uri + ">"))
    val jenaModel = ModelFactory.createDefaultModel
    val model = Model(jenaModel)
    val resource = ResourceFactory.createResource(uri)
    while (rs.hasNext) {
      val qs = rs.next
      val property = processProperty(qs)
      val predicate = processPredicate(qs)
      jenaModel.add(resource, property.property, predicate.rdfNode)
      predicate match {
        case r: RdfResource =>
          val subject = Some(LazyDataStore(r.uri, loadSubject))
          val predicate = Some(LazyDataStore(r.uri, loadPredicate))
          model.add(property, r, OptionalResultQuery(subject, predicate))
        case l: RdfLiteral => model.add(property, l)
        case a: RdfAnon => model.add(property, a)
        case _ => {}
      }
    }
    model
  }

  def loadPredicate(uri: String): InverseModel = {
    val rs = QueryEngine.performQuery (queryPredicate,Seq("<" + uri + ">"))
    
    val jenaModel = ModelFactory.createDefaultModel
    val model = InverseModel(jenaModel)
    val resource = ResourceFactory.createResource(uri)

    while (rs.hasNext) {
      val qs = rs.next
      val subject = processSubject(qs)
      val property = processProperty(qs)
      jenaModel.add(subject.resource, property.property, resource)

      val subjectAsResource = subject.asInstanceOf[RdfResource]
      val s = Some(LazyDataStore(subjectAsResource.uri, loadSubject))
      val p = Some(LazyDataStore(subjectAsResource.uri, loadPredicate))

      model.add(subject, property, OptionalResultQuery(s, p))
    }
    model
  }

  protected def processSubject(qs: QuerySolution): RdfResource = {
    val uri = qs.get("?s")
    val sl = qs.get("?sl")
    val label = if (sl == null) { None } else { Some(sl.toString) }

    RdfResource(UriFormatter.format(uri.toString), label, uri.asResource)
  }

  protected def processProperty(qs: QuerySolution): RdfProperty = {
    val uri = qs.get("?v").toString
    val vl = qs.get("?vl")
    val label = if (vl == null) { None } else { Some(vl.toString) }
    RdfProperty(UriFormatter.format(uri), label, ResourceFactory.createProperty(uri))
  }

  protected def processPredicate(qs: QuerySolution): RdfNode = {

    val uri = qs.get("?p")

    uri match {
      case a if a.isAnon() =>
        val vl = qs.get("?pl")
        val label = if (vl == null) { None } else { Some(vl.toString) }
        RdfAnon(label, a.asResource())
      case e if e.isLiteral() =>
        val literal = e.asLiteral
        val dataType = literal.getDatatype match {
          case dt if dt != null =>
            Some(UriFormatter.format(dt.getURI))
          case _ => None
        }
        RdfLiteral(literal.getValue.toString, dataType, uri)
      case e if e.isResource() =>
        val vl = qs.get("?pl")
        val label = if (vl == null) { None } else { Some(vl.toString) }
        RdfResource(UriFormatter.format(uri.toString), label, uri.asResource)
    }
  }

}