package es.weso.wesby

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import es.weso.wesby.utils.UriFormatter

@RunWith(classOf[JUnitRunner])
class UriFormatterSpec extends Specification {

  "UriFormatter converter" should {
    
    val localUri = "http://localhost:9000/ontology/Country"
    val baseUri = "http://data.webfoundation.org/ontology/Country"
    val nonManagedUri = "http://www.wikipedia.org/"

    "Convert to local URI a managed base URI" in new WithApplication {
      UriFormatter.uriToLocalURI(baseUri) must beEqualTo(localUri)
    }

    "Convert to local URI a managed local URI" in new WithApplication {
      UriFormatter.uriToLocalURI(localUri) must beEqualTo(localUri)
    }

    "Convert to base URI a managed local URI" in new WithApplication {
      UriFormatter.uriToBaseURI(localUri) must beEqualTo(baseUri)
    }

    "Convert to base URI a managed base URI" in new WithApplication {
      UriFormatter.uriToBaseURI(baseUri) must beEqualTo(baseUri)
    }

    "Convert to local URI a non-managed URI" in new WithApplication {
      UriFormatter.uriToLocalURI(nonManagedUri) must beEqualTo(nonManagedUri)
    }
    
    "Convert to base URI a non-managed URI" in new WithApplication {
      UriFormatter.uriToBaseURI(nonManagedUri) must be equalTo (nonManagedUri)
    }

  }
  
  "UriFormatter formatter" should {
    
    val countryLocalUri = "http://localhost:9000/webindex/v2013/country/BEN"
    val managedBaseUri = "http://data.webfoundation.org/ontology/WESO"
    val nonManagedUri = "http://www.w3.org/2004/02/skos/core#Concept"
    
    "Format a relative URI from a managed local URI without prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(countryLocalUri)
      formattedUri.relative must beEqualTo(countryLocalUri)
    }
    
    "Format a base URI from a managed local URI without prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(countryLocalUri)
      formattedUri.absolute must beEqualTo(countryLocalUri)
    }
    
    "Format a short URI from a managed local URI without prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(countryLocalUri)
      formattedUri.short must beNone
    }
    
    "Format a relative URI from a managed base URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      formattedUri.relative must beEqualTo("http://localhost:9000/ontology/WESO")
    }
    
    "Format a base URI from a managed base URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      formattedUri.absolute must beEqualTo(managedBaseUri)
    }
    
    "Format a short URI from a managed base URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      formattedUri.short must beSome
    }
    
    "Format the prefix URI from a managed absolute URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      val shortUri = formattedUri.short.get
      shortUri.prefix._1 must beEqualTo("http://localhost:9000/ontology/")
    }
    
    "Get the prefix from a managed absolute URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      val shortUri = formattedUri.short.get
      shortUri.prefix._2 must beEqualTo("wf-onto")
    }
    
    "Format the suffix URI from a managed absolute URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      val shortUri = formattedUri.short.get
      shortUri.suffix._1 must beEqualTo("http://localhost:9000/ontology/WESO")
    }
    
    "Get the suffix from a managed absolute URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(managedBaseUri)
      val shortUri = formattedUri.short.get
      shortUri.suffix._2 must beEqualTo("WESO")
    }

    "Format a relative URI from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      formattedUri.relative must beEqualTo(nonManagedUri)
    }
    
    "Format an absolute URI from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      formattedUri.absolute must beEqualTo(nonManagedUri)
    }
    
    "Format a short URI from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      formattedUri.short must beSome
    }
    
    "Format the prefix URI from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      val shortUri = formattedUri.short.get
      shortUri.prefix._1 must beEqualTo("http://www.w3.org/2004/02/skos/core#")
    }
    
    "Get the prefix from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      val shortUri = formattedUri.short.get
      shortUri.prefix._2 must beEqualTo("skos")
    }
    
    "Format the suffix URI from a non-managed URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      val shortUri = formattedUri.short.get
      shortUri.suffix._1 must beEqualTo(nonManagedUri)
    }
    
    "Get the suffix from a managed absolute URI with prefix" in new WithApplication {
      val formattedUri = UriFormatter.format(nonManagedUri)
      val shortUri = formattedUri.short.get
      shortUri.suffix._2 must beEqualTo("Concept")
    }
  }

}