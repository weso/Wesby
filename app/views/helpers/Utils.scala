package views.helpers

import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.ResultQuery
import es.weso.wfLodPortal.utils.CommonURIS._
import es.weso.wfLodPortal.models._
import play.api.templates.Html

object Utils {

  val Empty = ""

  def toUpper(text: String) = text.toUpperCase

  def toUpper(html: Html) = html.toString.toUpperCase

  def toLower(text: String) = text.toLowerCase

  def toLower(html: Html) = html.toString.toLowerCase

  def iso2(resultQuery: ResultQuery): String = {
    resultQuery.subject match {
      case Some(subject) =>
        subject.get(wfOnto, "has-iso-alpha2-code") match {
          case Some(iso2) => iso2.nodes.head match {
            case l: RdfLiteral => { l.value }
            case _ => Empty
          }
          case None => Empty
        }
      case None => Empty
    }
  }

  def iso3(resultQuery: ResultQuery): String = {
    resultQuery.subject match {
      case Some(subject) =>
        subject.get(wfOnto, "has-iso-alpha3-code") match {
          case Some(iso3) => iso3.nodes.head match {
            case l: RdfLiteral => { l.value }
            case _ => Empty
          }
          case None => Empty
        }
      case None => Empty
    }
  }

  def label(resultQuery: ResultQuery): String = {
    val label = resultQuery.subject match {
      case Some(subject) =>
        subject.get(rdfs, "label") match {
          case Some(labels) => filterLabels(labels.nodes.toList)
          case None => Empty
        }
      case None => Empty
    }
    label
  }

  def rdfType(resultQuery: ResultQuery): String = {
    resultQuery.subject match {
      case Some(subject) => {
        subject.get(rdf, "type") match {
          case Some(typeResult) => {
            typeResult.nodes.head match {
              case r: RdfResource => { r.uri.relative }
              case _ => { "Unknown rdf:type" }
            }
          }
          case _ => { "Unknown rdf:type" }
        }
      }
      case _ => { "Unknown rdf:type" }
    }
  }

  def rdfTypeLabel(resultQuery: ResultQuery): String = {
    resultQuery.subject match {
      case Some(subject) => {
        subject.get(rdf, "type") match {
          case Some(typeResult) => {
            typeResult.nodes.head match {
              case r: RdfResource => { label(r.dataStores) }
              case _ => Empty
            }
          }
          case None => Empty
        }
      }
      case None => Empty
    }
  }

  def showLabel(uri: String, label: String): String = {
    val chunks = label.split("@")
    if (chunks(0).isEmpty)
      uri
    else if (chunks.length > 1 && chunks(chunks.length - 1) == "en")
      chunks(0)
    else label
  }

  protected def filterLabels(nodes: List[RdfNode]): String = {
    val labels = for {
      label <- nodes
      text = label match {
        case l: RdfLiteral => l.value
        case _ => Empty
      }
      if !text.isEmpty
    } yield text

    labels.head
  }

}