package es.weso.wfLodPortal.sparql.custom

import scala.Array.canBuildFrom
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{ Map => MutableMap }

import com.hp.hpl.jena.query.QuerySolution

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.Uri
import es.weso.wfLodPortal.sparql.QueryEngine
import es.weso.wfLodPortal.utils.UriFormatter
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes

object ObservationCustomQuery extends Configurable {
  case class Observation(val uri: String, val name: String, val value: Float, val year: String)

  val queryObservations = conf.getString("query.observation.allYears")

  def loadObservations(uri: String, mode: String) = {
  	val rs = QueryEngine.performQuery(queryObservations, Seq(mode, uri))
  	
  	val list = ListBuffer[Observation]()

    while (rs.hasNext) {
      val qs = rs.next
      val uri = UriFormatter.uriToLocalURI(qs.getResource("?obs2").getURI)

      val label = qs.getLiteral("?label").getString
      val year = qs.getLiteral("?year").getString
      val value = qs.getLiteral("?value").getFloat
      
      val observation = Observation(uri, label, value, year)
      list += observation
    }
    list
  }
}