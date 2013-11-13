package views.helpers

import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.DataStore

object Handlers {

  def handleResourceAs[T](dataStore: DataStore, prefix: String, suffix: String, method: (RdfResource) => T): List[T] = {
    val indicators = dataStore.get(prefix, suffix) match {
      case Some(indicators) =>
        for {
          node <- indicators.nodes
        } yield {
          node match {
            case r: RdfResource => Some(method(r))
            case _ => None
          }
        }
      case None => List.empty
    }
    indicators.toList.flatten
  }

  def handleResourceAsString(dataStore: DataStore, prefix: String, suffix: String, method: (RdfResource) => String): String = {
    handleResourceAs[String](dataStore, prefix, suffix, method).mkString(" ")
  }

  def handleLiteralAs[T](dataStore: DataStore, prefix: String, suffix: String, method: (RdfLiteral) => T): List[T] = {
    val indicators = dataStore.get(prefix, suffix) match {
      case Some(indicators) =>
        for {
          node <- indicators.nodes
        } yield {
          node.asRdfLiteral match {
            case Some(l) =>
              Some(method(l))
            case _ => None
          }
        }
      case None => List.empty
    }
    indicators.toList.flatten
  }

  def handleLiteralAsString(dataStore: DataStore, prefix: String, suffix: String, method: (RdfLiteral) => String): String = {
    handleLiteralAs[String](dataStore, prefix, suffix, method).mkString(" ")
  }

  def handleLiteralAsValue(dataStore: DataStore, prefix: String, suffix: String): String = {
    val inner = (l: RdfLiteral) => l.value
    handleLiteralAsString(dataStore, prefix, suffix, inner)
  }
}