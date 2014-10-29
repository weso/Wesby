package es.weso.wesby.testCopies

import es.weso.wesby.models.Options
import scala.language.implicitConversions

/**
 * This class is a copy of es.weso.wesby.models.OptionsHelper and used ONLY
 * WITH TEST PURPOSES.
 */
class OptionsHelper(options: Options) {
  val customHost = options.host
  val customField = "I'm a custom field"
  def getFullUri = options.uri
}

object OptionsHelper {
  implicit def optionsWrapper(options:Options) = new OptionsHelper(options)
}