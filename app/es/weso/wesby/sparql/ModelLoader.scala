package es.weso.wesby.sparql

import com.hp.hpl.jena.query.{QueryParseException, QuerySolution}
import com.hp.hpl.jena.rdf.model.{ModelFactory, ResourceFactory}
import com.hp.hpl.jena.sparql.resultset.ResultSetException
import es.weso.wesby.Configurable
import es.weso.wesby.models.{InverseModel, LazyDataStore, Model, RdfAnon, RdfLiteral, RdfNode, RdfProperty, RdfResource, ResultQuery, Uri}
import es.weso.wesby.utils.UriFormatter
import es.weso.wesby.utils.UriFormatter.uriToBaseURI
import play.Logger

/**
 * Loads the Resource Model and InverseModel into Memory.
 */
object ModelLoader extends Configurable {

  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")
  val baseUri = conf.getString("sparql.baseuri")
  val actualUri = conf.getString("sparql.actualuri")
  val indexPath = conf.getString("sparql.index")

  val Subject = "?s"
  val Verb = "?v"
  val Predicate = "?p"

  /**
   * Loads a ResulQuery for the given URI,
   * it contains its children and parents.
   * @param uri the supplied URI
   */
  def loadUri(uri: String): ResultQuery = {
    val fullUri: Uri = UriFormatter.format(baseUri + uri)
    Logger.info("Uri: " + fullUri.absolute)
    val subject = LazyDataStore(fullUri, loadSubject)
    val predicate = LazyDataStore(fullUri, loadPredicate)
    ResultQuery(subject, predicate)
  }

  /**
   * Loads a ResulQuery for the composed URI,
   * it contains its children and parents.
   * @param prefix the supplied prefix
   * @param suffix the supplied suffix
   */
  def loadUri(sufix: String, preffix: String): ResultQuery = {
    val fullUri = UriFormatter.format(uriToBaseURI(sufix + preffix))
    Logger.info("Uri: " + fullUri.absolute)
    val subject = LazyDataStore(fullUri, loadSubject)
    val predicate = LazyDataStore(fullUri, loadPredicate)
    ResultQuery(subject, predicate)
  }

  /**
   * Loads a Model which contains its children.
   * @param uri the supplied URI
   */
  protected def loadSubject(uri: String): Model = {
    val jenaModel = ModelFactory.createDefaultModel
    val model = Model(jenaModel)
    try {
      val rs = QueryEngine.performQuery(querySubject, Seq(uri))
      val resource = ResourceFactory.createResource(uri)
      while (rs.hasNext) {
        val qs = rs.next
        val property = processProperty(qs, Verb)
        val predicate = processPredicate(qs)
        jenaModel.add(resource, property.property, predicate.rdfNode)
        model.add(property, predicate)
      }
    } catch {
      case e: QueryParseException =>
        Logger.warn("wesby was unable to query: '" + uri + "'")
      case e: ResultSetException =>
        Logger.warn("wesby was unable to process the resultSet '" + e.getMessage + "'")
    }
    model
  }

  /**
   * Loads an InverseModel which contains its parents.
   * @param uri the supplied URI
   */
  protected def loadPredicate(uri: String): InverseModel = {
    val jenaModel = ModelFactory.createDefaultModel
    val model = InverseModel(jenaModel)
    try {
      val rs = QueryEngine.performQuery(queryPredicate, Seq(uri))
      val resource = ResourceFactory.createResource(uri)
      while (rs.hasNext) {
        val qs = rs.next
        val vl = qs.get(Subject)
        val property = processProperty(qs, Verb)
        if (vl.isAnon) {
          val subject = RdfAnon(vl.asResource())
          jenaModel.add(subject.resource, property.property, resource)
          model.add(subject, property)
        } else {
          val subject = processResource(qs, Subject)
          jenaModel.add(subject.resource, property.property, resource)
          model.add(subject, property)
        }
      }
    } catch {
      case e: QueryParseException =>
        Logger.warn("wesby was unable to query: '" + uri + "'")
      case e: ResultSetException =>
        Logger.warn("wesby was unable to process the resultSet, uri: " +
          "'" + uri + "', message: '" + e.getMessage + "'")
    }
    model
  }

  /**
   * Process the predicate return a RdfNode.
   * The returned RdfNode may be an RdfProperty, an RdfResource or an RdfLiteral.
   * @param qs the target QuerySolution
   */
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

  /**
   * Process the the query solution returning a RdfResource
   * @param qs the target QuerySolution
   * @param param Name of the resource field
   */
  protected def processResource(qs: QuerySolution, param: String): RdfResource = {
    val vl = qs.get(param)
    val uri = UriFormatter.format(vl.asResource.getURI)
    val dss = processResultQuery(uri)
    RdfResource(uri, dss, vl.asResource)
  }

  /**
   * Process the the query solution returning a RdfProperty
   * @param qs the target QuerySolution
   * @param param Name of the property field
   */
  protected def processProperty(qs: QuerySolution, param: String): RdfProperty = {
    val uri = UriFormatter.format(qs.get(param).asResource.getURI)
    val dss = processResultQuery(uri)
    val property = ResourceFactory.createProperty(uri.absolute)
    RdfProperty(uri, dss, property)
  }

  /**
   * Generates a ResultQuery for a given URI
   * @param uri the supplied URI
   */
  protected def processResultQuery(uri: Uri): ResultQuery = {
    val subject = Some(LazyDataStore(uri, loadSubject))
    val predicate = Some(LazyDataStore(uri, loadPredicate))
    ResultQuery(subject, predicate)
  }

}