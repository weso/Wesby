package es.weso.wesby

import es.weso.wesby.sparql.{Handlers, ModelLoader}
import es.weso.wesby.utils.CommonURIS._
import org.specs2.mutable.Specification
import play.api.test.WithApplication

class HandlersSpec extends Specification {
  
  "Handlers" should {

    "do nothing when" >> {
      "A Resource is expected but a Literal is supplied" in new WithApplication {
        val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        // rdfs:label is a Literal
        val output = Handlers.handleResourceAsString(ds, rdfs, "label", (x) => x.uri.relative)
        output must beEqualTo("")
      }

      "A Literal is expected but a Resource is supplied" in new WithApplication {
        val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        // rdf:type is a Resource
        val output = Handlers.handleLiteralAsString(ds, rdf, "type", (x) => x.value)
        output must beEqualTo("")
      }
    }

    "handle the first" >> {
      "resource of a list" in new WithApplication {
        val ds = ModelLoader.loadUri(wfOnto, "WESO").predicate.get
        val output = Handlers.handleFirstResourceAs(ds, dcterms, "contributor", (x) => x.uri.short.get.suffix._2)
        val expected = "CZE"
        output.get must beEqualTo(expected)
      }

      "literal of only one (as Value)" in new WithApplication {
        val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val output = Handlers.handleFirstLiteralAsValue(ds, rdfs, "label")
        output must beEqualTo("Spain")
      }
    }

    "handle all Resources" >> {
      "of only one" in new WithApplication {
        val ds = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val output = Handlers.handleResourceAsString(ds, dcterms, "publisher", (x) => x.uri.relative)
        val expected = "http://localhost:9000/organization/WebFoundation"
        output must beEqualTo(expected)
      }

      "of a list (with a custom Separator)" in new WithApplication {
        val ds = ModelLoader.loadUri(wiRegion, "Americas").subject.get
        val output = Handlers.handleResourceAsString(ds, wfOnto, "has-country", (x) => x.uri.short.get.suffix._2, ",")
        val expected = "CAN,CHL,JAM,PER,USA,BRA,CRI,MEX,COL,VEN,URY,ECU,ARG"
        output must beEqualTo(expected)
      }
    }
  }
}