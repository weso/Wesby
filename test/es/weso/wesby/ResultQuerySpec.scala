package es.weso.wesby

import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.CommonURIS._
import org.specs2.mutable.Specification
import play.api.test.WithApplication

class ResultQuerySpec extends Specification {
  
  "ResultQuery" should {
    "Navigate to its children" in new WithApplication {
      val rq = ModelLoader.loadUri("webindex/v2013/region/Africa")
      val children = rq.subject
      children must beSome
    }
    
    "Navigate to its parents" in new WithApplication {
      val rq = ModelLoader.loadUri(wiRegion, "Europe")
      val parents = rq.predicate
      parents must beSome
    }
    
    "Return an empty list when the Resource has no parents" in new WithApplication {
      val res = ModelLoader.loadUri("http://en.wikipedia.org/wiki/", "RDF")
      val parents = res.predicate.get.list
      parents.size must beEqualTo(0)
    }
    
    "Return a populated list when it has parents" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "CZE")
      val parents = rq.predicate.get.list
      parents.size must beGreaterThan(0)
    }
    
    "Return a populated list when it has children" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "CZE")
      val children = rq.subject.get.list
      children.size must beGreaterThan(0)
    }
  }

}