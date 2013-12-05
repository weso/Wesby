package es.weso.wfLodPortal

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

trait Configurable {
  val conf = new CompositeConfiguration
  conf.append(new PropertiesConfiguration("conf/endpoint.properties"))
  conf.append(new PropertiesConfiguration("conf/queries.properties"))
  conf.append(new PropertiesConfiguration("conf/prefixes.properties"))
  conf.append(new PropertiesConfiguration("conf/comparer.properties"))
}