package es.weso.wesby

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

/**
 * Adds the basic configuration capability.
 */
trait Configurable {
  val conf = new CompositeConfiguration
  conf.append(new PropertiesConfiguration("conf/wesby/endpoint.properties"))
  conf.append(new PropertiesConfiguration("conf/wesby/queries.properties"))
}