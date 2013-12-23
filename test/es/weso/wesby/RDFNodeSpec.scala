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
        val res = getExampleRdfResource
        res.asRdfLiteral must beNone
      }
      "RDFResource to RDFAnon must be None" in new WithApplication {
        val res = getExampleRdfResource
        res.asRdfAnon must beNone
      }
      "RDFResource to RDFProperty must be None" in new WithApplication {
        val res = getExampleRdfResource
        res.asRdfProperty must beNone
      }
      "RDFResource to RDFResource must be Some" in new WithApplication {
        val res = getExampleRdfResource
        res.asRdfResource must beSome
      }
      "RDFLiteral to RDFLiteral must be Some" in new WithApplication {
        val lit = getExampleTypedRdfLiteral
        lit.asRdfLiteral must beSome
      }
      "RDFLiteral to RDFAnon must be None" in new WithApplication {
        val lit = getExampleUntypedRdfLiteral
        lit.asRdfAnon must beNone
      }
      "RDFLiteral to RDFProperty must be None" in new WithApplication {
        val lit = getExampleTypedRdfLiteral
        lit.asRdfProperty must beNone
      }
      "RDFLiteral to RDFResource must be None" in new WithApplication {
        val lit = getExampleUntypedRdfLiteral
        lit.asRdfResource must beNone
      }
      "RDFProperty to RDFLiteral must be None" in new WithApplication {
        val prop = getExampleRdfProperty
        prop.asRdfLiteral must beNone
      }
      "RDFProperty to RDFAnon must be None" in new WithApplication {
        val prop = getExampleRdfProperty
        prop.asRdfAnon must beNone
      }
      "RDFProperty to RDFProperty must be Some" in new WithApplication {
        val prop = getExampleRdfProperty
        prop.asRdfProperty must beSome
      }
      "RDFProperty to RDFResource must be None" in new WithApplication {
        val prop = getExampleRdfProperty
        prop.asRdfResource must beNone
      }
    }
  }

  "RDFResource" should {
    "Return its URI" in new WithApplication {
      val rdfRes = getExampleRdfResource
      rdfRes.u.absolute must beEqualTo(wfOnto + "Country")
    }
    "Navigate to its parents" in new WithApplication {
      val rdfRes = getExampleRdfResource
      val parents = rdfRes.dataStores.predicate.get.list
      parents.size must beGreaterThan(0)
    }
    "Navigate to its children" in new WithApplication {
      val rdfRes = getExampleRdfResource
      val children = rdfRes.dataStores.subject.get.list
      children.size must beGreaterThan(0)
    }
  }

  "RDFLiteral" should {
    "Return its value" in new WithApplication {
      val rdfLit = getExampleUntypedRdfLiteral
      rdfLit.value must beEqualTo("United States of America")
    }
    "Return its type" >> {
      "Must be Some when it has type" in new WithApplication {
        val rdfLit = getExampleTypedRdfLiteral
        rdfLit.dataType must beSome
      }
      "The type URI must be correct" in new WithApplication {
        val rdfLit = getExampleTypedRdfLiteral
        rdfLit.dataType.get.absolute must beEqualTo(xsd + "date")
      }
      "Must be None when it doesn't have type" in new WithApplication {
        val rdfLit = getExampleUntypedRdfLiteral
        rdfLit.dataType must beNone
      }
    }
  }

  "RDFProperty" should {
    "Return its URI" in new WithApplication {
      val rdfProp = getExampleRdfProperty
      rdfProp.u.absolute must beEqualTo(rdf + "type")
    }
    "Not have any parents" in new WithApplication {
      val rdfProp = getExampleRdfProperty
      val parents = rdfProp.dataStores.predicate.get.list
      parents must have size (0)
    }
  }

  /**
   * @return An example RDFProperty to use in the tests.
   * The property is rdf:type
   */
  def getExampleRdfProperty: RdfProperty = {
    val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
    // Get the property with tag rdf:type
    val prop = rq.get(rdf + "type")
    prop.get.p
  }

  /**
   * @return An example RDFResource to use in the tests.
   * The resource is rdf:type
   */
  def getExampleRdfResource: RdfResource = {
    val rq = ModelLoader.loadUri(wiCountry, "ESP").subject.get
    Handlers.handleFirstResourceAs(rq, rdf, "type", x => x).get
  }

  /**
   * @return An example RDFLiteral with type to use in the tests.
   * The literal is dcterms:issued, with type xsd:label (value= 2013-12-11)
   */
  def getExampleTypedRdfLiteral: RdfLiteral = {
    val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
    Handlers.handleFirstLiteralAs(rq, dcterms, "issued", x => x).get
  }

  /**
   * @return An example RDFLiteral whithout type to use in the tests.
   * The literal is rdfs:label (value= United States of America)
   */
  def getExampleUntypedRdfLiteral: RdfLiteral = {
    val rq = ModelLoader.loadUri(wiCountry, "USA").subject.get
    Handlers.handleFirstLiteralAs(rq, rdfs, "label", x => x).get
  }
}