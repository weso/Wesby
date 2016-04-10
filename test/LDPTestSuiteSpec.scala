import java.util

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.w3.ldp.testsuite.LdpTestSuite
import play.Logger

import play.api.test._
import play.api.test.Helpers._

/**
 * Created by jorge on 20/7/15.
 */
@RunWith(classOf[JUnitRunner])
class LDPTestSuiteSpec extends Specification {
  // TODO LDP test suite

//  "Application" should {
//    "pass the LDP test suite" in new WithServer() {
//      // Default port: 19001
//
//      //configure test suite
////      Logger.info("Running W3C official LDP Test Suite")
////      val options = new util.HashMap[String, String]()
////      options.put("server", "http://localhost:19001")
//
////      val ldpTestSuite = new LdpTestSuite(options)
////      ldpTestSuite.run()
////      Logger.debug(ldpTestSuite.getStatus.toString)
//
////      log.info("Running W3C official LDP Test Suite against '{}' server", baseUrl);
////      Map<String, String> options = new HashMap<>();
////      options.put("server", container);
////      options.put("basic", null);
////      options.put("non-rdf", null);
////      options.put("cont-res", resource);
////      if (!LdpService.SERVER_MANAGED_PROPERTIES.isEmpty()) {
////        options.put("read-only-prop", LdpService.SERVER_MANAGED_PROPERTIES.iterator().next().stringValue());
////      }
////      options.put("httpLogging", null);
////      options.put("skipLogging", null);
////      options.put("excludedGroups", "MANUAL");
//    }
//  }
}
