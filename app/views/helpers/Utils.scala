package views.helpers

import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfNode
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.ResultQuery
import es.weso.wfLodPortal.sparql.Handlers.handleFirstLiteralAsValue
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAsString
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.rdfs
import es.weso.wfLodPortal.utils.CommonURIS.wfOnto
import play.api.templates.Html
import views.helpers.wf.Utils.cachedLabel

object Utils {

  val Empty = ""

  def toUpper(text: String) = text.toUpperCase

  def toUpper(html: Html) = html.toString.toUpperCase

  def toLower(text: String) = text.toLowerCase

  def toLower(html: Html) = html.toString.toLowerCase

  def iso2(resultQuery: ResultQuery): String = {
    handleFirstLiteralAsValue(resultQuery.subject.get,
      wfOnto, "has-iso-alpha2-code")
  }

  def iso3(resultQuery: ResultQuery): String = {
    handleFirstLiteralAsValue(resultQuery.subject.get,
      wfOnto, "has-iso-alpha3-code")
  }

  def rdfType(resultQuery: ResultQuery): String = {
    val result = handleResourceAsString(resultQuery.subject.get,
      rdf, "type",
      (r: RdfResource) => { r.uri.relative })
    if (result.isEmpty)
      "Unknown rdf:type"
    else result
  }

  def rdfTypeLabel(resultQuery: ResultQuery): String = {
    handleResourceAsString(resultQuery.subject.get,
      rdf, "type",
      (r: RdfResource) => cachedLabel(r.dataStores))
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

    if (labels.length > 0) labels.head; else ""
  }

}