package es.weso.wesby.models

import scala.language.implicitConversions

/**
 * Extends an Options object
 * @param options the options to be extended
 */
class OptionsHelper(options: Options) {
  //Add here your custom attributes and/or methods
}

/**
 * OptionsHelper Companion Object
 */
object OptionsHelper {
  
  /**
   * Receive and options object and returns its OptionsHelper associated.
   * @param options the options to be extended
   */
  implicit def optionsWrapper(options:Options) = new OptionsHelper(options)
}