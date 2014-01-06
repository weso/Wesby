package app.models

import es.weso.wesby.models.Options

/**
 * Extends an Options object
 * @param options the options to be extended
 */
class OptionsHelper(options: Options) {
  //Add here your custom attributes and/or methods
}

/**
 * OptionsHeelper Companion Object
 */
object OptionsHelper {
  
  /**
   * Receive and options object and returns its OptionsHelper associated.
   * @param options the options to be extended
   */
  implicit def optionsWrapper(options:Options) = new OptionsHelper(options)
}