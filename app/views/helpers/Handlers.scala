package views.helpers

import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfResource
import scala.Option.option2Iterable

object Handlers {

  def handleAsLiteral[T](dataStore: es.weso.wfLodPortal.models.DataStore, prefix: String, suffix: String, method: es.weso.wfLodPortal.models.RdfLiteral => T) = {
    val indicators = dataStore.get(prefix, suffix) match {
      case Some(indicators) =>
        for {
          node <- indicators.nodes
        } yield {
          node match {
            case r: RdfLiteral =>
              Some(method(r))
            case _ => None
          }
        }
      case None => List.empty
    }
    indicators.toList.flatten.mkString(" ")
  }

  def handleAsResource[T](dataStore: es.weso.wfLodPortal.models.DataStore, prefix: String, suffix: String, method: es.weso.wfLodPortal.models.RdfResource => T) = {
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
    indicators.toList.flatten.mkString(" ")
  }

  def handleAsValue(dataStore: es.weso.wfLodPortal.models.DataStore, prefix: String, suffix: String) = {
    val inner = (l: RdfLiteral) => l.value
    handleAsLiteral[String](dataStore, prefix, suffix, inner)
  }
}