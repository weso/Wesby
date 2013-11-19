package es.weso.wfLodPortal.sparql.custom

import scala.Array.canBuildFrom
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{ Map => MutableMap }
import com.hp.hpl.jena.query.QuerySolution
import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.sparql.QueryEngine
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import es.weso.wfLodPortal.utils._
import es.weso.wfLodPortal.utils.UriFormatter._
import es.weso.wfLodPortal.models.Uri

object RootQueries extends Configurable {
  val queries = List(
  	("root.webindex.ranking", "root.webindex.ranking.description"),
  	("root.webindex.allIndicators", "root.webindex.allIndicators.description"),
  	("root.webindex.allIndicatorsByComponentAndIndex", "root.webindex.allIndicatorsByComponentAndIndex.description"),
  	("root.webindex.allCountries", "root.webindex.allCountries.description")
  )

  def loadQueries() = {
  	val list = new ListBuffer[scala.collection.mutable.Map[String,Object]]()
  
  	for(query <- queries) { 
  		val options:scala.collection.mutable.Map[String,Object] = HashMap.empty
  		options("query") = conf.getString(query._1)
  		options("description") = conf.getString(query._2) 
  		options("endpoint") = conf.getString("sparql.endpoint")
  		
  		list += options
  	}
  	list
  }
}