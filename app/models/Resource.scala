package models

import org.w3.banana._

/**
 * Created by jorge on 6/10/15.
 */
class Resource[Rdf <: RDF](
  uri: Rdf#URI,
  label: String//,
  //  shapes: List[Rdf#URI],
  //  properties: List[Rdf#Node],
  //  inverseProperties: List[Rdf#Node]
  )(implicit ops: RDFOps[Rdf]) {

}
