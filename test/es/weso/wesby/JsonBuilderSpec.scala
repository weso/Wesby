package es.weso.wesby

import es.weso.wesby.sparql.ModelLoader
import es.weso.wesby.utils.JsonBuilder
import org.specs2.mutable.Specification
import play.api.test.WithApplication

/**
 * Created by jorge on 08/07/14.
 */
class JsonBuilderSpec extends Specification {
  "JsonBuilder" should {
    "Generate JSON from a ResultQuery that is not empty" in new WithApplication {
      val rq = ModelLoader.loadUri("webindex/v2013/region/Africa")
      val json = JsonBuilder.toJson(rq)
      json.values must not beEmpty
    }
  }

  "JsonBuilder" should {
    "Return a JSON document that has a cachedLabel property" in new WithApplication {
      val rq = ModelLoader.loadUri("webindex/v2013/region/Africa")
      val json = JsonBuilder.toJson(rq)
      json \ "cachedLabel" must beEqualTo("\"Africa\"")
    }
  }

}
