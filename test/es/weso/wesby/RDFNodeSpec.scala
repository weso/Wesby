package es.weso.wesby

import scala.collection.Seq
import org.specs2.mutable.Specification
import org.specs2.specification.Fragments
import org.specs2.main.ArgProperty
import play.api.test.WithApplication
import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.CommonURIS._
import es.weso.wesby.models.RdfResource
import es.weso.wesby.sparql.Handlers
import es.weso.wesby.models._

class RDFNodeSpec extends Specification {

  "RDFNode" should {
    "perform safe casts between it's subtypes" >> {
      "RDFResource to RDFLiteral must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val res = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
        res.asRdfLiteral must beNone
      }
      "RDFResource to RDFAnon must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val res = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
        res.asRdfAnon must beNone
      }
      "RDFResource to RDFProperty must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val res = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
        res.asRdfProperty must beNone
      }
      "RDFResource to RDFResource must be Some" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val res = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
        res.asRdfResource must beSome
      }
      "RDFLiteral to RDFLiteral must be Some" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val lit = Handlers.handleFirstAs(rq, rdfs, "label", (x: RdfNode) => x).get
        lit.asRdfLiteral must beSome
      }
      "RDFLiteral to RDFAnon must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val lit = Handlers.handleFirstAs(rq, rdfs, "label", (x: RdfNode) => x).get
        lit.asRdfAnon must beNone
      }
      "RDFLiteral to RDFProperty must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val lit = Handlers.handleFirstAs(rq, rdfs, "label", (x: RdfNode) => x).get
        lit.asRdfProperty must beNone
      }
      "RDFLiteral to RDFResource must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val lit = Handlers.handleFirstAs(rq, rdfs, "label", (x: RdfNode) => x).get
        lit.asRdfResource must beNone
      }
      "RDFProperty to RDFLiteral must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val prop = rq.get(rdf + "type")
        val rdfProp = prop.get.p
        rdfProp.asRdfLiteral must beNone
      }
      "RDFProperty to RDFAnon must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val prop = rq.get(rdf + "type")
        val rdfProp = prop.get.p
        rdfProp.asRdfAnon must beNone
      }
      "RDFProperty to RDFProperty must be Some" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val prop = rq.get(rdf + "type")
        val rdfProp = prop.get.p
        rdfProp.asRdfProperty must beSome
      }
      "RDFProperty to RDFResource must be None" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
        val prop = rq.get(rdf + "type")
        val rdfProp = prop.get.p
        rdfProp.asRdfResource must beNone
      }
    }
  }

  "RDFResource" should {
    "Return its URI" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      // The resource type is wfOnto:Country
      val rdfNode = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
      val rdfRes = rdfNode.asRdfResource.get
      rdfRes.u.absolute must beEqualTo(wfOnto + "Country")
    }
    "Navigate to its parents" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      val rdfNode = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
      val rdfRes = rdfNode.asRdfResource.get
      val parents = rdfRes.dataStores.predicate.get.list
      parents.size must beGreaterThan(0)
    }
    "Navigate to its children" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      val rdfNode = Handlers.handleFirstAs(rq, rdf, "type", (x: RdfNode) => x).get
      val rdfRes = rdfNode.asRdfResource.get
      val children = rdfRes.dataStores.subject.get.list
      children.size must beGreaterThan(0)
    }
  }

  "RDFLiteral" should {
    "Return its value" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
      val rdfLit = Handlers.handleFirstLiteralAs(rq, rdfs, "label", x => x).get
      rdfLit.value must beEqualTo("United States of America")
    }
    "Return its type" >> {
      "Must be Some when it has type" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
        val rdfLit = Handlers.handleFirstLiteralAs(rq, dcterms, "issued", x => x).get
        rdfLit.dataType must beSome
      }
      "The type URI must be correct" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
        val rdfLit = Handlers.handleFirstLiteralAs(rq, dcterms, "issued", x => x).get
        rdfLit.dataType.get.absolute must beEqualTo(xsd + "date")
      }
      "Must be None when it doesn't have type" in new WithApplication {
        val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
        val rdfLit = Handlers.handleFirstLiteralAs(rq, rdfs, "label", x => x).get
        rdfLit.dataType must beNone
      }
    }
  }

  "RDFProperty" should {
    "Return its URI" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      // Get the rdf:type property
      val prop = rq.get(rdf +"type")
      val rdfProp = prop.get.p
      // The property url must be the same as before (rdf:type)
      rdfProp.u.absolute must beEqualTo(rdf +"type")
    }
    "Not have any parents" in new WithApplication {
      val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
      val prop = rq.get(rdf +"type")
      val rdfProp = prop.get.p
      val parents = rdfProp.dataStores.predicate.get.list
      parents must have size(0)
    }
  }
}