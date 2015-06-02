package es.weso.wesby.models

import com.hp.hpl.jena.rdf.model.{Property => JenaProperty, RDFNode => JenaRDFNode, Resource => JenaResource}

/**
 * Wesby's representation of an RDF node.
 */
sealed abstract class RdfNode {
  val rdfNode: JenaRDFNode

  /**
   * Returns a Jena's RdfNode.
   */
  def r: JenaRDFNode = rdfNode

  /**
   * Performs a safe casting to RdfAnon.
   */
  def asRdfAnon: Option[RdfAnon] = {
    this match {
      case e: RdfAnon => Some(e)
      case _ => None
    }
  }

  /**
   * Performs a safe casting to RdfResource.
   */
  def asRdfResource: Option[RdfResource] = this match {
    case e: RdfResource => Some(e)
    case _ => None
  }

  /**
   * Performs a safe casting to RdfPropety.
   */
  def asRdfProperty: Option[RdfProperty] = this match {
    case p: RdfProperty => Some(p)
    case _ => None
  }

  /**
   * Performs a safe casting to RdfLiteral.
   */
  def asRdfLiteral: Option[RdfLiteral] = this match {
    case l: RdfLiteral => Some(l)
    case _ => None
  }

}

/**
 * Wesby's representation of an RDF anonymous node.
 * @param rdfNode the Jena's RdfNode.
 */
case class RdfAnon(
  rdfNode: JenaResource) extends RdfNode {

  /**
   * Returns a Jena's RdfNode.
   */
  def resource = rdfNode

  override def toString: String = {
    new StringBuilder("A[id:'").append(rdfNode.getId()).append("']").toString
  }
}

/**
 * Trait that allows to navigate to its children and parents.
 */
trait Resource {
  
  /**
   * the node's ResultQuery
   */
  val dataStores: ResultQuery

  /**
   * the resource's ResultQuery (Alias of dataStores)
   */
  def dss: ResultQuery = dataStores

}

/**
 * Wesby's representation of an RDF resource.
 * @param uri the resource's URI
 * @param dataStores the resource's ResultQuery
 * @param rdfNode the Jena's Resource
 */
case class RdfResource(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaResource) extends RdfNode with Resource {

  /**
   * Returns the resource's URI (Alias of uri)
   */
  def u = uri
  
  /**
   * Returns the Jena's Resource
   */
  def resource = rdfNode

  override def toString: String = {
    new StringBuilder("R[uri:'").append(uri).append("']").toString
  }
}

/**
 * Wesby's representation of an RDF property.
 * @param uri the property's URI
 * @param dataStores the property's ResultQuery
 * @param rdfNode the Jena's Property
 */
case class RdfProperty(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaProperty) extends RdfNode with Resource {

  /**
   * Returns the property's URI (Alias of uri)
   */
  def u = uri
  
  /**
   * Returns the Jena's Property
   */
  def property: JenaProperty = rdfNode

  override def toString: String = {
    new StringBuilder("P[uri:'").append(uri).append("']").toString
  }
}

/**
 * Wesby's representation of an RDF literal.
 * @param value the literal's value
 * @param dataType the (optional) literal's data-type 
 * @param rdfNode the Jena's RdfNode
 */
case class RdfLiteral(
  val value: String,
  val dataType: Option[Uri],
  rdfNode: JenaRDFNode) extends RdfNode {

  /**
   * Returns a Jena's RdfNode (Alias of r).
   */
  def literal: JenaRDFNode = rdfNode

  override def toString: String = {
    new StringBuilder("L[Value:'").append(value).append("', Data Type:'")
      .append(dataType).append("']").toString
  }

}