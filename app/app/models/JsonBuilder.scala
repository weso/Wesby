package app.models

import es.weso.wesby.models._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import views.helpers.Utils._

import scala.collection.mutable.ListBuffer

/**
 * Created by jorge on 27/06/14.
 */
object JsonBuilder {

  def toJson(resultQuery: ResultQuery): JsObject = {
    Json.obj(
      "cachedLabel" -> JsString(cachedLabel(resultQuery)),
      "rdfType" -> JsString(rdfType(resultQuery)),
      "rdfTypeLabel" -> JsString(rdfTypeLabel(resultQuery)),
      "subjects" -> {
        resultQuery.subject match {
          case Some(subject) => toJsArray(subject)
          case None => {
            JsNull
          }
        }
      },
      "predicates" -> {
        resultQuery.predicate match {
          case Some(predicate) => toJsArray(predicate)
          case None => {
            JsNull
          }
        }
      }
    )
  }

  private def toJsArray(nodes: DataStore): JsValueWrapper = {
    var properties: ListBuffer[JsObject] = ListBuffer()

    nodes.list.foreach(
      p => properties += Json.obj(
        "property" -> toJsonNode(p.property),
        "values" -> {
          var nodes: ListBuffer[JsObject] = ListBuffer()

          p.nodes.foreach(
            n => nodes += Json.obj("value" -> toJsonNode(n))
          )
          JsArray(nodes)
        }
      )
    )
    JsArray(properties)

  }

  private def toJsonNode(n: RdfNode): JsObject = {

    n match {
      case r: RdfResource => resourceToUri(r)
      case p: RdfProperty => propertyToUri(p)
      case l: RdfLiteral => literalToValue(l)
      case a: RdfAnon => {
        Json.obj("value" -> JsString("1 anonymous resource " + a.rdfNode.getId))
      }
    }
  }

  def literalToValue(n: RdfLiteral): JsObject = {
    n.dataType match {
      case Some(dataType) => {
        dataType.short match {
          case Some(short) => {
            Json.obj(
              "typedLiteral" -> Json.obj(
                "value" -> JsString(n.value),
                "prefixUri" -> short.prefix._1,
                "prefixLabel" -> short.prefix._2,
                "suffixUri" -> short.suffix._1,
                "suffixLabel" -> short.suffix._2
              ))
          }
          case None => {
            Json.obj("simpleLiteral" -> JsString(dataType.absolute))
          }
        }
      }
      case None => {
        Json.obj("simpleLiteral" -> JsString(n.value))
      }
    }
  }

  def resourceToUri(n: RdfResource): JsObject = {
    cachedLabel(n).trim match {
      case label if !label.isEmpty => {
        Json.obj(
          "label" -> showLabel(n.uri.absolute, label),
          "uri" -> n.uri.relative
        )
      }
      case _ => {
        n.uri.short match {
          case Some(short) => {
            Json.obj(
              "shortUri" -> Json.obj(
                "prefixUri" -> short.prefix._1,
                "prefixLabel" -> short.prefix._2,
                "suffixUri" -> short.suffix._1,
                "suffixLabel" -> short.suffix._2
              ))
          }
          case None => {
            Json.obj("label" -> n.uri.absolute)
          }
        }
      }
    }
  }

  def propertyToUri(n: RdfProperty): JsObject = {
    cachedLabel(n).trim match {
      case label if !label.isEmpty => {
        Json.obj(
          "label" -> showLabel(n.uri.absolute, label),
          "uri" -> n.uri.relative
        )
      }
      case _ => {
        n.uri.short match {
          case Some(short) => {
            Json.obj(
              "shortUri" -> Json.obj(
                "prefixUri" -> short.prefix._1,
                "prefixLabel" -> short.prefix._2,
                "suffixUri" -> short.suffix._1,
                "suffixLabel" -> short.suffix._2
              ))
          }
          case None => {
            Json.obj("none" -> n.uri.absolute)
          }
        }
      }
    }
  }

}
