package es.weso.wfLodPortal.sparql.custom

import scala.Array.canBuildFrom
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{Map => MutableMap}
import com.hp.hpl.jena.query.QuerySolution
import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.sparql.QueryEngine
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import es.weso.wfLodPortal.utils.UriFormatter
import es.weso.wfLodPortal.models.Uri

object IndicatorCustomQuery extends Configurable {
  case class Indicator(val uri: Uri, val code: String, var description: String, observations: ListBuffer[Observation])
  case class Observation(val uri: Uri, val value: Float, val year: Float, val country: Country)
  case class Country(uri: Uri, val label: String, iso3: String)

  val queryCountries = conf.getString("query.compare")

  implicit val countryReads = Json.reads[Country]
  implicit val countryWrites = Json.writes[Country]

  implicit val observationReads = Json.reads[Observation]
  implicit val observationWrites = Json.writes[Observation]

  implicit val indicatorReads = Json.reads[Indicator]
  implicit val indicatorWrites = Json.writes[Indicator]

  def loadObservations(regions: Array[String], years: Array[String], indicators: Array[String]) = {
    val regionFilter = regions.map("(?iso3 = \"" + _ + "\"^^xsd:string)").mkString(" || ")
    val yearFilter = years.map("(?year = " + _ + ")").mkString(" || ")
    val indicatorFilter = indicators.map("(?indicatorCode  = \"" + _ + "\"^^xsd:string)").mkString(" || ")
    val rs = QueryEngine.performQuery(queryCountries, List(indicatorFilter, yearFilter, regionFilter))

    val map: MutableMap[String, Indicator] = HashMap.empty

    while (rs.hasNext) {
      val qs = rs.next
      val uri = UriFormatter.format(qs.getResource("?indicator").getURI)
      val code = qs.getLiteral("?indicatorCode").getString
      val description = qs.getLiteral("?definition").getString

      val indicator = map.getOrElse(code, Indicator(uri, code, description, ListBuffer.empty))
      
      if (description.length > indicator.description.length)
      	indicator.description = description

      indicator.observations += loadObservation(qs, indicator)
      map += code -> indicator
    }
    Map()++map
  }

  protected def loadObservation(qs: QuerySolution, indicator: Indicator) = {
    val uri = UriFormatter.format(qs.getResource("?obs").getURI())
    val value = qs.getLiteral("?value").getFloat
    val year = qs.getLiteral("?year").getFloat
    Observation(uri, value, year, loadCountry(qs))
  }

  protected def loadCountry(qs: QuerySolution) = {
    val uri = UriFormatter.format(qs.getResource("?country").getURI)
    val label = qs.getLiteral("?countryLabel").getString
    val iso3 = qs.getLiteral("?iso3").getString
    Country(uri, label, iso3)
  }
}