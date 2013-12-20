package es.weso.wesby

import scala.collection.Seq
import org.specs2.mutable.Specification
import org.specs2.specification.Fragments
import org.specs2.main.ArgProperty
import play.api.test.WithApplication
import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.CommonURIS
import es.weso.wesby.models.RdfResource
import es.weso.wesby.sparql.Handlers
import es.weso.wesby.models.RdfNode
import es.weso.wesby.models.RdfAnon
import es.weso.wesby.models.RdfLiteral
import es.weso.wesby.models.RdfProperty
import es.weso.wesby.models.RdfAnon

class RDFNodeSpec extends Specification {

  "RDFNode" should {
    "perform safe casts between it's subtypes" >> {
      "RDFResource to" >> {
        "RDFLiteral must be None" in new WithApplication {
          val res = getAResource
          res.asRdfLiteral must beNone
        }

        "RDFAnon must be None" in new WithApplication {
          val res = getAResource
          res.asRdfAnon must beNone
        }

        "RDFProperty must be None" in new WithApplication {
          val res = getAResource
          res.asRdfProperty must beNone
        }

        "RDFResource must be Some" in new WithApplication {
          val res = getAResource
          res.asRdfResource must beSome
        }
      }

      "RDFLiteral to" >> {
        "RDFLiteral must be Some" in new WithApplication {
          val res = getALiteral
          res.asRdfLiteral must beSome
        }

        "RDFAnon must be None" in new WithApplication {
          val res = getALiteral
          res.asRdfAnon must beNone
        }

        "RDFProperty must be None" in new WithApplication {
          val res = getALiteral
          res.asRdfProperty must beNone
        }

        "RDFResource must be None" in new WithApplication {
          val res = getALiteral
          res.asRdfResource must beNone
        }
      }

    }

  }

  def getAResource = {
    val rq = ModelLoader.loadUri(CommonURIS.wiCountry, "ESP").subject.get
    Handlers.handleFirstAs(rq, CommonURIS.rdf, "type", (x: RdfNode) => x).get
  }

  def getALiteral = {
    val rq = ModelLoader.loadUri(CommonURIS.wiCountry, "ESP").subject.get
    Handlers.handleFirstAs(rq, CommonURIS.rdfs, "label", (x: RdfNode) => x).get
  }

}