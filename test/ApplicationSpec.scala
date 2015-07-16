import javax.swing.text.html.HTML

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

//    "send 404 on a bad request" in new WithApplication{
//      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
//    } TODO

    "redirect home to /welcome when using the test endpoint" in new WithApplication() {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(SEE_OTHER)
      redirectLocation(home) must beSome.which(_ == "/welcome")
    }

    "perform dereferencing in" in new WithApplication() {

      val resource = route(FakeRequest(GET, "/resource/test").withHeaders(ACCEPT -> "text/html")).get
      status(resource) must equalTo(SEE_OTHER)
      redirectLocation(resource) must beSome.which(_ == "/resource/test.html")

      val resource2 = route(FakeRequest(GET, "/resource/test").withHeaders(ACCEPT -> "text/plain")).get
      status(resource2) must equalTo(SEE_OTHER)
      redirectLocation(resource2) must beSome.which(_ == "/resource/test.txt")

    }

    "perform content negotiation" in new WithApplication() {

      val resource = route(FakeRequest(GET, "/resource/test.html").withHeaders(ACCEPT -> "text/html")).get
      status(resource) must equalTo(OK)
      contentType(resource) must beSome("text/html")
      charset(resource) must beSome("utf-8")

      val resource2 = route(FakeRequest(GET, "/resource/test.txt").withHeaders(ACCEPT -> "text/plain")).get
      status(resource2) must equalTo(OK)
      contentType(resource2) must beSome("text/plain")
      charset(resource) must beSome("utf-8")

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
