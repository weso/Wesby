import javax.swing.text.html.HTML

import org.specs2.matcher.MatchResult
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/resource/boum.rdf")) must beSome.which (status(_) == NOT_FOUND)
    }

    "redirect home to /welcome when using the test endpoint" in new WithApplication() {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)
      redirectLocation(home) must beSome.which(_ == "/welcome")
    }

    "perform dereferencing in" in new WithApplication() {

      dereference("html", "text/html")
      dereference("ttl", "text/turtle")
      dereference("jsonld", "application/ld+json")
      dereference("txt", "text/plain")
      dereference("rdf", "application/rdf+xml")
      dereference("nt", "application/n-triples")
//      dereference("n3", "text/n3")

      def dereference(extension: String, mimeType: String): MatchResult[Option[String]] = {
        val resource = route(FakeRequest(GET, "/Bob").withHeaders(ACCEPT -> mimeType)).get
        status(resource) must equalTo(SEE_OTHER)
        redirectLocation(resource) must beSome.which(_ == s"/Bob.$extension")
      }


    }

    "perform content negotiation" in new WithApplication() {

      negotiate("html", "text/html")
      negotiate("ttl", "text/turtle")
      negotiate("jsonld", "application/ld+json")
      negotiate("txt", "text/plain")
      negotiate("rdf", "application/rdf+xml")
      negotiate("nt", "application/n-triples")
//      negotiate("n3", "text/n3")

      private def negotiate(extension: String, mimeType: String): MatchResult[Option[String]] = {
        val resource = route(FakeRequest(GET, s"/bob.$extension").withHeaders(ACCEPT -> mimeType)).get
        status(resource) must equalTo(OK)
        contentType(resource) must beSome(mimeType)
        charset(resource) must beSome("utf-8")
      }
    }

//    "render the index page" in new WithApplication{
//      val home = route(FakeRequest(GET, "/")).get
//
//      status(home) must equalTo(OK)
//      contentType(home) must beSome.which(_ == "text/html")
//      contentAsString(home) must contain ("Your new application is ready.")
//    }
  }
}
