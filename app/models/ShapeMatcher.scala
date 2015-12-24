package models

import es.weso.rdf.{PrefixMap, RDFReader}
import es.weso.rdfgraph.nodes.{IRI, RDFNode}
import es.weso.shacl.{Schema => ShaclSchema, Label, ShaclResult, ShaclMatcher}
import play.Play
import play.api.Logger

import scala.util.{Failure, Success, Try}

object ShapeMatcher {

  val schemaFormat = Play.application().configuration().getString("shapes.format")
  val shaclSchema: Try[(ShaclSchema, PrefixMap)] = ShaclSchema.fromFile(
    Play.application().configuration().getString("shapes.location"),
    schemaFormat
  )

  def matchWithShacl(rdf: RDFReader, node: String) : List[String] = {
    val labels: List[Label] = shaclSchema.get._1.labels
    Logger.debug("LABELS: " + labels)
    val results = for (label <- labels) yield matchLabelWithShacl(rdf, node, label)
    results.flatten
  }

  def matchLabelWithShacl(rdf: RDFReader, node: String, label: Label) = {
    val result = for (
          (schema, pm) <- shaclSchema
        ) yield {
            val validator = ShaclMatcher(schema, rdf)
            val r = validator.match_node_label(IRI(node))(label)
            (r, pm)
          }

        result match {
          case Success((validationResult: ShaclResult, pm)) => {
            val r = validationResult.show(0)(pm)

            if (r.equals("<No results>")) None else Option(label.toString)

          }
          case Failure(f) => {
            Logger.debug("Matching failed: " + f)
            Logger.debug("Node: " + node)
            Logger.debug("RDF: " + rdf)
            None
          }
        }
  }

  private def nodeToString(node: RDFNode): String = {
    if (node.isIRI) node.toIRI.str
    else node.toString
  }

  private def nodesToString(nodes: Set[RDFNode]): String = {
    val sb = new StringBuilder
    for (node <- nodes) {
      sb.append(nodeToString(node))
    }
    sb.append("\n")
    sb.toString()
  }
}