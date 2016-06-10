package models

import com.hp.hpl.jena.graph.{Node, Graph => JenaGraph}
import es.weso.utils.JenaUtils
import org.w3.banana._
import org.w3.banana.io.{RDFWriter, Turtle}
import org.w3.banana.jena.{Jena, JenaModule}
import play.api.libs.json.{JsResult, JsSuccess, JsValue}

import scala.Int
import scalaz.Digit._2

//import scala.collection.immutable.Iterable
import scala.util.Try


/**
 * Created by jorge on 6/10/15.
 */
case class Resource[Rdf<:RDF](
  uri: Rdf#URI,
  labels: Iterable[Rdf#Literal],
  shapes: List[String],//List[Rdf#URI],
  properties: Map[Rdf#URI, Iterable[Rdf#Node]],
  inverseProperties: Iterable[(Rdf#URI, Rdf#URI)]
//  val inverseProperties: Iterable[(Rdf#Node, Rdf#URI)]
  ) extends RDFModule with RDFOpsModule with JenaModule
//  with Format[Resource[Jena]]
{
  import ops._
  def label = labels.headOption
}

object Resource {
  import play.api.libs.json._
  implicit object ResourceFormat extends Format[Resource[Jena]] {
      override def writes(r: Resource[Jena]): JsValue = {

        val common: JsObject = Json.obj(
          "@context" -> "./context",
          "@id" -> r.uri.toString()
        )

        val props = for(p <- r.properties) yield p._1.getLocalName -> p._2.map(n =>
          if (n.isURI) n.getURI
          else if (n.isLiteral) n.getLiteral.getLexicalForm
          else n.toString
        )

        val reverseProps = r.inverseProperties
          .map(p => (p._2.getLocalName, p._1.getURI))
          .groupBy(_._1)
          .mapValues(_.map(_._2))
//          .mapValues(_.map(_._2).toSeq.sortWith((s1: String, s2: String) => {
//            val id1 = s1.substring(s1.lastIndexOf("/") + 1, s1.length)
//            val id2 = s2.substring(s2.lastIndexOf("/") + 1, s2.length)
//
//            if(id1.forall(_.isDigit) && id2.forall(_.isDigit)) id1.toInt < id2.toInt
//            else s1 < s2
//          }))
          .map {
            case (k, v) => k -> (for (u <- v) yield Json.obj("@id" -> u))
          }

        val jsonReverse = Json.toJson(reverseProps)
        val json: JsValue = Json.toJson(props)

        common ++ json.as[JsObject] + ("@reverse" -> jsonReverse)
      }

      override def reads(json: JsValue): JsResult[Resource[Jena]] = {
        val res: models.Resource[Jena] = ResourceBuilderWithJena.build("", JenaGraph.emptyGraph, List(""))
        JsSuccess(res)
      }
  }
}



