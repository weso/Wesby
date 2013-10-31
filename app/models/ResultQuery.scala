package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

case class ResultQuery(subject: Model, predicate: InverseModel)

case class Model(
  val jenaModel: JenaModel) extends DataStore[Model] {

  def add(p: RdfProperty, r: RdfResource, lds: LazyDataStore[Model]) { addToDataStore(p, r, Some(lds)) }

  def add(p: RdfProperty, l: RdfLiteral) { addToDataStore(p, l, None) }

  def add(p: RdfProperty, a: RdfAnon) { addToDataStore(p, a, None) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }

}

case class InverseModel(
  val jenaModel: JenaModel) extends DataStore[InverseModel] {

  def add(r: RdfResource, p: RdfProperty, lds: LazyDataStore[InverseModel]) { addToDataStore(p, r, Some(lds)) }

  def add(l: RdfLiteral, p: RdfProperty, lds: Option[LazyDataStore[InverseModel]]) { addToDataStore(p, l, lds) }

  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }
}

trait DataStore[T] {

  protected val map: HashMap[String, Property[T]] = HashMap.empty

  protected def addToDataStore(p: RdfProperty, n: RdfNode, lds: Option[LazyDataStore[T]]) {
    val m = map.getOrElse(p.uri.relative, Property(p))
    m.nodes += EnrichNode(n, lds)
    map += p.uri.relative -> m
  }

  def get(uri: String): Option[Property[T]] = {
    map.get(uri)
  }

  def list = map.valuesIterator.toList

}

case class Property[T](val property: RdfProperty) {
  val nodes: ListBuffer[EnrichNode[T]] = ListBuffer.empty[EnrichNode[T]]
}

case class EnrichNode[T](val node: RdfNode, val dataStore: Option[LazyDataStore[T]])

case class LazyDataStore[T](uri: Uri, method: (String) => T) {
  lazy val dataStore = method(uri.absolute)
  def data = dataStore
}