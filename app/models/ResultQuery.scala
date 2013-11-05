package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

case class ResultQuery(subject: Model, predicate: InverseModel) {
  val s = subject
  val p = predicate
}

trait DataStore[T] {

  protected val map: HashMap[String, Property[T]] = HashMap.empty

  protected def addToDataStore(p: RdfProperty, n: RdfNode, lds: OptionalResultQuery) {
    val m = map.getOrElse(p.uri.relative, Property(p))
    m.nodes += Node(n, lds)
    map += p.uri.relative -> m
  }

  def get(base: String, suffix: String): Option[Property[T]] = {
    get(base + suffix)
  }
  
  def get(uri: String): Option[Property[T]] = {
    map.get(uri)
  }

  def list = map.valuesIterator.toList

}

case class Model(
  val jenaModel: JenaModel) extends DataStore[Model] {

  def add(p: RdfProperty, r: RdfResource, lds: OptionalResultQuery) { addToDataStore(p, r, lds) }

  def add(p: RdfProperty, l: RdfLiteral) { addToDataStore(p, l, OptionalResultQuery(None, None)) }

  def add(p: RdfProperty, a: RdfAnon) { addToDataStore(p, a, OptionalResultQuery(None, None)) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }

}

case class InverseModel(
  val jenaModel: JenaModel) extends DataStore[InverseModel] {

  def add(r: RdfResource, p: RdfProperty, lds: OptionalResultQuery) { addToDataStore(p, r, lds) }

  def add(l: RdfLiteral, p: RdfProperty, lds: OptionalResultQuery) { addToDataStore(p, l, lds) }

  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }
}

case class Property[T](val property: RdfProperty) {
  val nodes: ListBuffer[Node[T]] = ListBuffer.empty[Node[T]]
  val p = property
  val ns = nodes
}

case class Node[T](val node: RdfNode, val dataStores: OptionalResultQuery) {
  val n = node
  val dss = dataStores
}

case class OptionalResultQuery(val subject: Option[LazyDataStore[Model]], val predicate: Option[LazyDataStore[InverseModel]]) {
  val s = subject
  val p = predicate
}

case class LazyDataStore[T](val uri: Uri, val method: (String) => T) {
  private lazy val dataStore = method(uri.absolute)
  def data = dataStore
  def d = dataStore
  val u = uri
  val m = method
}