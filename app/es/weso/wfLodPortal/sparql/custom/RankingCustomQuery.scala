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

object RankingCustomQuery extends Configurable {
  case class Country(uri: Uri, val name: String, iso2: String, iso3: String, value: String)

  val queryRanking = conf.getString("query.ranking.allCountries")

  implicit val countryReads = Json.reads[Country]
  implicit val countryWrites = Json.writes[Country]

  def loadRanking(mode: String) = {
    val rs = QueryEngine.performQuery(queryRanking, Seq(mode))

    val countries = new ListBuffer[Country]()
    
    val values = new ListBuffer[String]()

    while (rs.hasNext) {
      val qs = rs.next
      val uri = qs.getResource("?country").getURI
      val iso2 = qs.getLiteral("?iso2").getString
      val iso3 = qs.getLiteral("?iso3").getString
      val label = qs.getLiteral("?label").getString
      val value = qs.getLiteral("?value").getString

      val country = Country(UriFormatter.format(uri), label, iso2, iso3, value)

      countries += country
      values += label
    }
    Map("series" -> countries, "values" -> values)
  }
}