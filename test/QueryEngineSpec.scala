import java.net.URL

import com.hp.hpl.jena.query.ResultSet
import models.{QueryEngineDependencies, QueryEngineWithJena}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.w3.banana.RDF
import play.Play

import play.api.test._
import play.api.test.Helpers._

/**
 * Created by jorge on 16/7/15.
 */
@RunWith(classOf[JUnitRunner])
class QueryEngineSpec[Rdf <: RDF] extends Specification {

  "QueryEngine" should {

      "load the test endpoint" in new WithApplication() {
        val endpoint = new URL(Play.application().configuration().getString("wesby.endpoint"))
        QueryEngineWithJena.endpoint must beEqualTo(endpoint)
      }

      "query for triples" >> {
        "in wich Asturias is the subject" in new WithApplication() {
          val path = "resource/Asturias"
          val resource = Play.application().configuration().getString("wesby.datasetBase") + path
          val query = Play.application().configuration().getString("queries.s")
//          val solutions: Rdf#Solutions = QueryEngineWithJena.select(resource, query)
//
////          val pred: Iterable[String] = solutions.
        }
      }


  }
}
