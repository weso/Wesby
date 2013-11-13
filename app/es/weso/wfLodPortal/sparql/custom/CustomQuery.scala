package es.weso.wfLodPortal.sparql.custom

import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.InverseModel
import es.weso.wfLodPortal.models.DataStore

trait CustomQuery {

  protected def firstNodeAsResource(dataStore: Model, property: String): Option[RdfResource] = {
    val literal = dataStore.get(property)
    literal match {
      case Some(iso2) => iso2.nodes.head match {
        case l: RdfResource => Some(l)
        case _ => None
      }
      case None => None
    }
  }

  protected def firstNodeAsResource(dataStore: Model, prefix: String, suffix: String): Option[RdfResource] = {
    firstNodeAsResource(dataStore: Model, prefix + suffix)
  }

  protected def firstNodeAsLiteral(dataStore: Model, property: String): String = {
    val literal = dataStore.get(property)
    literal match {
      case Some(iso2) => iso2.nodes.head match {
        case l: RdfLiteral => l.value
        case _ => "Undefined"
      }
      case None => "Undefined"
    }
  }

  protected def firstNodeAsLiteral(dataStore: Model, prefix: String, suffix: String): String = {
    firstNodeAsLiteral(dataStore, prefix + suffix)
  }

  protected def handleResource[T](dataStore: DataStore, prefix: String, suffix: String, method: RdfResource => T): List[T] = {
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

  protected def handleLiteral[T](dataStore: DataStore, prefix: String, suffix: String, method: RdfLiteral => T): List[T] = {
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
    indicators.toList.flatten
  }

}