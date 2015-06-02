package es.weso.wesby

import es.weso.wesby.utils.UriFormatter
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class UriFormatterSpec extends Specification {

  "UriFormatter" should {

    "convert" >> {

      val localUri = "http://localhost:9000/ontology/Country"
      val baseUri = "http://data.webfoundation.org/ontology/Country"
      val nonManagedUri = "http://www.wikipedia.org/"

      "convert to relative URI" >> {
        "a managed base URI" in new WithApplication {
          UriFormatter.uriToLocalURI(baseUri) must beEqualTo(localUri)
        }
        "a managed relative URI" in new WithApplication {
          UriFormatter.uriToLocalURI(localUri) must beEqualTo(localUri)
        }
        "a non-managed URI" in new WithApplication {
          UriFormatter.uriToLocalURI(nonManagedUri) must beEqualTo(nonManagedUri)
        }
      }

      "convert to base URI" >> {
        "a managed relative URI" in new WithApplication {
          UriFormatter.uriToBaseURI(localUri) must beEqualTo(baseUri)
        }
        "a managed base URI" in new WithApplication {
          UriFormatter.uriToBaseURI(baseUri) must beEqualTo(baseUri)
        }
        "a non-managed URI" in new WithApplication {
          UriFormatter.uriToBaseURI(nonManagedUri) must be equalTo (nonManagedUri)
        }
      }
    }

    "format" >> {

      val countryLocalUri = "http://localhost:9000/webindex/v2013/country/BEN"
      val managedBaseUri = "http://data.webfoundation.org/ontology/WESO"
      val nonManagedUri = "http://www.w3.org/2004/02/skos/core#Concept"

      "format a relative URI from" >> {
        "a managed relative URI without prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(countryLocalUri)
          formattedUri.relative must beEqualTo(countryLocalUri)
        }
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          formattedUri.relative must beEqualTo("http://localhost:9000/ontology/WESO")
        }
        "a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          formattedUri.relative must beEqualTo(nonManagedUri)
        }
      }

      "format a base URI from" >> {
        "a managed relative URI without prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(countryLocalUri)
          formattedUri.absolute must beEqualTo(countryLocalUri)
        }
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          formattedUri.absolute must beEqualTo(managedBaseUri)
        }
        "a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          formattedUri.absolute must beEqualTo(nonManagedUri)
        }
      }

      "format a short URI from" >> {
        "a managed relative URI without prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(countryLocalUri)
          formattedUri.short must beNone
        }
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          formattedUri.short must beSome
        }
        "Format a short URI from a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          formattedUri.short must beSome
        }
      }

      "format the prefix URI from" >> {
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          val shortUri = formattedUri.short.get
          shortUri.prefix._1 must beEqualTo("http://localhost:9000/ontology/")
        }
        "a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          val shortUri = formattedUri.short.get
          shortUri.prefix._1 must beEqualTo("http://www.w3.org/2004/02/skos/core#")
        }

      }

      "format the suffix URI from" >> {
        "a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          val shortUri = formattedUri.short.get
          shortUri.suffix._1 must beEqualTo(nonManagedUri)
        }
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          val shortUri = formattedUri.short.get
          shortUri.suffix._1 must beEqualTo("http://localhost:9000/ontology/WESO")
        }
      }
    }

    "get" >> {
      val managedBaseUri = "http://data.webfoundation.org/ontology/WESO"
      val nonManagedUri = "http://www.w3.org/2004/02/skos/core#Concept"

      "get the prefix from" >> {
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          val shortUri = formattedUri.short.get
          shortUri.prefix._2 must beEqualTo("wf-onto")
        }
        "Get the prefix from a non-managed URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          val shortUri = formattedUri.short.get
          shortUri.prefix._2 must beEqualTo("skos")
        }
      }

      "get the suffix from" >> {
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(managedBaseUri)
          val shortUri = formattedUri.short.get
          shortUri.suffix._2 must beEqualTo("WESO")
        }
        "a managed base URI with prefix" in new WithApplication {
          val formattedUri = UriFormatter.format(nonManagedUri)
          val shortUri = formattedUri.short.get
          shortUri.suffix._2 must beEqualTo("Concept")
        }
      }
    }

  }
}