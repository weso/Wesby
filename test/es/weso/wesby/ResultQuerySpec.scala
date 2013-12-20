package es.weso.wesby

import scala.collection.Seq
import org.specs2.mutable.Specification
import org.specs2.specification.Fragments
import org.specs2.main.ArgProperty
import play.api.test.WithApplication
import es.weso.wesby.models.ResultQuery
import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.CommonURIS._
import es.weso.wesby.sparql.Handlers

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
    
//    "Be None when the Resource has no parents" in new WithApplication {
//      val res = ModelLoader.loadUri("http://en.wikipedia.org/wiki/", "RDF")
//      val parents = res.predicate
////      Fails
//      parents must beNone
////      Passes
////      parents.get.list.size must beEqualTo(0)
//    }
//    
//    "Be None when the Resource has no childern" in new WithApplication {
//    }
    
  }

}