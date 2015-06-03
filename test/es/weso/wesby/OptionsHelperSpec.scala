package es.weso.wesby

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import es.weso.wesby.testCopies.OptionsHelper._
import models.Options

class OptionsHelperSpec extends Specification {

  "OptionsHelper" should {

    "Make an implicit conversion from an Options" in new WithApplication {
      val options: Options = new Options("partialUri")
      // The implicit conversion wraps the Options into an OptionsHelper
      options.customField must beEqualTo("I'm a custom field")
    }

    "Access an Options method from an OptionsHelper field" in new WithApplication {
      val options = new Options("partialUri")
      options.customHost must beEqualTo(options.host)
    }

    "Access an Options field from an OptionsHelper method" in new WithApplication {
      val options = new Options("an uri")
      options.getFullUri must beEqualTo(options.baseUri + options.partialUri)
    }
  }
}
