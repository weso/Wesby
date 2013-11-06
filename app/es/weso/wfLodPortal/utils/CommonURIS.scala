package es.weso.wfLodPortal.utils

import es.weso.wfLodPortal.Configurable

object CommonURIS extends Configurable {

  protected val base = conf.getString("sparql.actualuri")

  val cex = "http://purl.org/weso/ontology/computex#"
  val dbpedia = "http://dbpedia.org/resource/"
  val dcterms = "http://purl.org/dc/terms/"
  val geo = "http://www.w3.org/2003/01/geo/wgs84_pos#"
  val odb = base + "odb/v2013/"
  val odbComponent = base + "odb/v2013/component/"
  val odbComputation = base + "odb/v2013/computation/"
  val odbCountry = base + "odb/v2013/country/"
  val odbDataset = base + "odb/v2013/dataset/"
  val odbIndex = base + "odb/v2013/index/"
  val odbIndicator = base + "odb/v2013/indicator/"
  val odbObs = base + "odb/v2013/observation/"
  val odbRanking = base + "odb/v2013/ranking/"
  val odbRegion = base + "odb/v2013/region/"
  val odbSlice = base + "odb/v2013/slice/"
  val odbWeightSchema = base + "odb/v2013/weightSchema/"
  val owl = "http://www.w3.org/2002/07/owl#"
  val qb = "http://purl.org/linked-data/cube#"
  val rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val rdfs = "http://www.w3.org/2000/01/rdf-schema#"
  val sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#"
  val sdmxCode = "http://purl.org/linked-data/sdmx/2009/code#"
  val sdmxConcept = "http://purl.org/linked-data/sdmx/2009/concept#"
  val sdmxSubject = "http://purl.org/linked-data/sdmx/2009/subject#"
  val skos = "http://www.w3.org/2004/02/skos/core#"
  val test = base + "test/v2013/"
  val time = "http://www.w3.org/2006/time#"
  val webindex = base + "webindex/v2013/"
  val wfOnto = base + "ontology/"
  val wfOrg = base + "organization/"
  val wfPeople = base + "people/"
  val wiComponent = base + "webindex/v2013/component/"
  val wiComputation = base + "webindex/v2013/computation/"
  val wiCountry = base + "webindex/v2013/country/"
  val wiCataset = base + "webindex/v2013/dataset/"
  val wiIndex = base + "webindex/v2013/index/"
  val wiIndicator = base + "webindex/v2013/indicator/"
  val wiObs = base + "webindex/v2013/observation/"
  val wiRanking = base + "webindex/v2013/ranking/"
  val wiRegion = base + "webindex/v2013/region/"
  val wiSlice = base + "webindex/v2013/slice/"
  val wiWeightSchema = base + "webindex/v2013/weightSchema/"
  val xsd = "http://www.w3.org/2001/XMLSchema#"

  def p(base: String, prefix: String) = base + prefix
  def u(base: String, prefix: String) = UriFormatter.format(base + prefix)
  def u(uri: String) = UriFormatter.format(uri)
}