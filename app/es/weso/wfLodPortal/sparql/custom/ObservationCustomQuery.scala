package es.weso.wfLodPortal.sparql.custom

import scala.collection.mutable.ListBuffer

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.sparql.QueryEngine
import es.weso.wfLodPortal.utils.UriFormatter

object ObservationCustomQuery extends CustomQuery with Configurable {
  
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