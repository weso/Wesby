package es.weso.wfLodPortal

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

import scala.collection.mutable.HashMap

object Prefixes extends Configurable {
  conf.append(new PropertiesConfiguration("conf/prefixes.properties"))
  
  val actualUri = conf.getString("sparql.actualuri")
  
  val prefixes = HashMap[String, String]();
  
  val it = conf.getKeys

  while (it.hasNext) {
  	val label = it.next()
  	val prefix = conf.getString(label).replace("<HOST>", actualUri)

  	prefixes(prefix) = label;	
  }
  
  def replacePrefix(uri: String, prefix: String, ending: String) : Option[String] = {
  	if (prefixes contains prefix)
  		return Option(prefixes.get(prefix).get + ":" + ending)
  	
  	return Option(uri)
  }
}