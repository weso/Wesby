package es.weso.wfLodPortal.utils

import es.weso.wfLodPortal.Configurable

object CommonURIS extends Configurable {

  /*
   * Place here the commons uris used by your project's custom views.
   * 
   * As a piece of advise, load the prefixes from conf/prefixes.ttl with
   * the helper UriFormatter.
   */

  val cex = UriFormatter.namespaces.get("cex").get
  val dbpedia = UriFormatter.namespaces.get("dbpedia").get
  val dcterms = UriFormatter.namespaces.get("dcterms").get
  val geo = UriFormatter.namespaces.get("geo").get
  val odb = UriFormatter.namespaces.get("odb").get
  val odbComponent = UriFormatter.namespaces.get("odb-component").get
  val odbComputation = UriFormatter.namespaces.get("odb-computation").get
  val odbCountry = UriFormatter.namespaces.get("odb-country").get
  val odbDataset = UriFormatter.namespaces.get("odb-dataset").get
  val odbIndex = UriFormatter.namespaces.get("odb-index").get
  val odbIndicator = UriFormatter.namespaces.get("odb-indicator").get
  val odbObs = UriFormatter.namespaces.get("odb-obs").get
  val odbRanking = UriFormatter.namespaces.get("odb-ranking").get
  val odbRegion = UriFormatter.namespaces.get("odb-region").get
  val odbSlice = UriFormatter.namespaces.get("odb-slice").get
  val odbWeightSchema = UriFormatter.namespaces.get("odb-weightSchema").get
  val owl = UriFormatter.namespaces.get("owl").get
  val qb = UriFormatter.namespaces.get("qb").get
  val rdf = UriFormatter.namespaces.get("rdf").get
  val rdfs = UriFormatter.namespaces.get("rdfs").get
  val sdmxAttribute = UriFormatter.namespaces.get("sdmxAttribute").get
  val sdmxCode = UriFormatter.namespaces.get("sdmxCode").get
  val sdmxConcept = UriFormatter.namespaces.get("sdmxConcept").get
  val sdmxSubject = UriFormatter.namespaces.get("sdmxSubject").get
  val skos = UriFormatter.namespaces.get("skos").get
  val test = UriFormatter.namespaces.get("test").get
  val time = UriFormatter.namespaces.get("time").get
  val webindex = UriFormatter.namespaces.get("webindex").get
  val wfOnto = UriFormatter.namespaces.get("wf-onto").get
  val wfOrg = UriFormatter.namespaces.get("wf-org").get
  val wfPeople = UriFormatter.namespaces.get("wf-people").get
  val wiComponent = UriFormatter.namespaces.get("wi-component").get
  val wiComputation = UriFormatter.namespaces.get("wi-computation").get
  val wiCountry = UriFormatter.namespaces.get("wi-country")
  val wiDataset = UriFormatter.namespaces.get("wi-dataset")
  val wiIndex = UriFormatter.namespaces.get("wi-index").get
  val wiIndicator = UriFormatter.namespaces.get("wi-indicator").get
  val wiObs = UriFormatter.namespaces.get("wi-obs").get
  val wiRanking = UriFormatter.namespaces.get("wi-ranking").get
  val wiRegion = UriFormatter.namespaces.get("wi-region").get
  val wiSlice = UriFormatter.namespaces.get("wi-slice").get
  val wiWeightSchema = UriFormatter.namespaces.get("wi-weightSchema").get
  val xsd = UriFormatter.namespaces.get("xsd").get
  
}