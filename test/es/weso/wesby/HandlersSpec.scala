package es.weso.wesby

import org.specs2.mutable.Specification

import es.weso.wesby.models.RdfLiteral
import es.weso.wesby.models.RdfResource
import es.weso.wesby.sparql.Handlers
import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.CommonURIS._
import play.api.test.WithApplication

class HandlersSpec extends Specification {

  "Handlers" should {
    "Handle a Resource as String when a Resource is supplied" in new WithApplication {
      val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      val handled = Handlers.handleResourceAsString(ds, dcterms, "publisher", (x) => x.uri.relative)
      val expected = "http://localhost:9000/organization/WebFoundation"
      handled must beEqualTo(expected)
    }
    
    "Not handle a Resource as String when a Literal is supplied" in new WithApplication {
      val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      val handled = Handlers.handleResourceAsString(ds, rdfs, "label", (x) => x.uri.relative)
      // Do nothing as we've supplied a Literal instead a Resource
      val expected = ""
      handled must beEqualTo(expected)
    }
  }
}