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
  case class Country(uri: Uri, val name: String, iso2: String, iso3: String, var value: Double)
  case class Ranking(series:ListBuffer[Country], var max: Double, var min: Double, values: ListBuffer[String])

  val queryRanking = conf.getString("query.ranking.allCountries")

  implicit val countryReads = Json.reads[Country]
  implicit val countryWrites = Json.writes[Country]

  def loadRanking(mode: String) = {
    val rs = QueryEngine.performQuery(queryRanking, Seq(mode))

    val countries = new ListBuffer[Country]()
    
    val values = new ListBuffer[String]()
    
    var min:Double = 0
    var max:Double = 0
    
    var rankings: MutableMap[String, Ranking] = HashMap.empty

    while (rs.hasNext) {
      val qs = rs.next
      val uri = qs.getResource("?country").getURI
      val iso2 = qs.getLiteral("?iso2").getString
      val iso3 = qs.getLiteral("?iso3").getString
      val label = qs.getLiteral("?label").getString
      val year = qs.getLiteral("?year").getString
      val _value = qs.getLiteral("?value").getString
      
      val ranking = if (rankings.contains(year)) {
        rankings(year)
      } else {
      	val ranking = Ranking(new ListBuffer[Country](), 0, 0, new ListBuffer[String]())
        rankings += year -> ranking
        ranking
      }
      
      val value = try {
      	_value.toDouble
      }
      catch {
      	case _ => 0
      }

      if (value < ranking.min)
      	ranking.min = value
      	
      if (value > ranking.max)
      	ranking.max = value

      val country = Country(UriFormatter.format(uri), label, iso2, iso3, value)

      ranking.series += country
      ranking.values += label
    }
    
    for(r <- rankings) {
    	val ranking = r._2
	    val difference = ranking.max - ranking.min
	    
	    for (country <- ranking.series) {
	    	country.value = (country.value - ranking.min) * 100 / difference
	    }
    }
    
    rankings
  }
}