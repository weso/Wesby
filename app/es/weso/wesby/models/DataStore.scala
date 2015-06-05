package es.weso.wesby.models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

/**
 * Stores the data for a given URI (Parents or children).
 */
trait DataStore {

  protected val map: Map[String, Property] = HashMap.empty

  /**
   * Adds a 'triple' to the DataStore
   * @param p the property to be added
   * @param n the RDF node to be added to the property
   */
  protected def addToDataStore(p: RdfProperty, n: RdfNode): Unit = {
    val m = map.getOrElse(p.uri.absolute, Property(p))
    m.nodes += n
    map += p.uri.absolute -> m
  }

  /**
   * Returns the property associated to the given URI
   * @param uri the property's URI
   */
  def get(uri: Uri): Option[Property] = {
    get(uri.absolute)
  }

  /**
   * Returns the property associated to the given URI
   * @param uri the property's URI
   */
  def get(uri: String): Option[Property] = {
    map.get(uri)
  }

  /**
   * Returns the property associated to the given URI (base + suffix)
   * @param base  the URI's prefix
   * @param suffix the URI's suffix
   */
  def get(base: String, suffix: String): Option[Property] = {
    get(base + suffix)
  }

  /**
   * Returns the list of properties.
   */
  def list = map.valuesIterator.toList

}

/**
 * DataStore specialized in storing the children of an RDF Resource.
 */
case class Model(
  val jenaModel: JenaModel) extends DataStore {

  /**
   * Adds a 'triple' to the DataStore
   * @param p the property to be added
   * @param r the RDF node to be added to the property
   */
  def add(p: RdfProperty, r: RdfNode) { addToDataStore(p, r) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }

}

/**
 * DataStore specialized in store the parents of an RDF Resource.
 */
case class InverseModel(
  val jenaModel: JenaModel) extends DataStore {

  /**
   * Adds a 'triple' to the DataStore
   * @param r the RDF node to be added to the property
   * @param p the property to be added
   */
  def add(r: RdfNode, p: RdfProperty) { addToDataStore(p, r) }

  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }
}

/**
 * Wesby's Property which contains all the RdfNodes associated to the property.
 * @param property the RdfProperty to be encapsulated in the Property
 */
case class Property(val property: RdfProperty) {
  val nodes: ListBuffer[RdfNode] = ListBuffer.empty[RdfNode]
  def p = property
  def ns = nodes
}

/**
 * A lazy DataStore loader.
 * @tparam T a sub-type from DataStore
 * @param uri the URI of the resource to load
 * @param method the callback method that retrieves the DataStore
 */
case class LazyDataStore[T <: DataStore](val uri: Uri, val method: (String) => T) {

  /**
   * Apply the method to the URI retrieving a DataStore
   */
  protected lazy val dataStore = method(uri.absolute)

  def data = dataStore
  def d = dataStore
  def u = uri
  def m = method

}