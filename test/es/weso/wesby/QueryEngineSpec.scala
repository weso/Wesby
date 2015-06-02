package es.weso.wesby

import es.weso.wesby.sparql.QueryEngine
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication

@RunWith(classOf[JUnitRunner])
class QueryEngineSpec extends Specification with Configurable {

  val sample = """test sample for subsitution {0}
		  whathever whathever {1}
		  still testing... {2} and {0}""""
  val querySubject = conf.getString("query.subject")
  val queryPredicate = conf.getString("query.predicate")

  "QueryEngine" should {
    "apply filters with" >> {
      "a single argument to the subject query" in new WithApplication {
        val subs = "term"
        val output = QueryEngine.applyFilters(querySubject, List(subs))
        val expected = querySubject.replace("{0}", subs)

        output must beEqualTo(expected)
      }

      "a single argument to the predicate query" in new WithApplication {
        val subs = "testing"
        val output = QueryEngine.applyFilters(queryPredicate, List(subs))
        val expected = queryPredicate.replace("{0}", subs)

        output must beEqualTo(expected)
      }

      "with multiple arguments to a multiline sample query" in new WithApplication {
        val subs = List("a", "b", "c")
        val output = QueryEngine.applyFilters(sample, subs)
        val expected = sample.replace("{0}", subs(0)).replace("{1}", subs(1)).replace("{2}", subs(2))

        output must beEqualTo(expected)
      }
    }
    
  }

}