package models

import com.google.common.base.Charsets
import com.google.common.io.Files
import es.weso.rdf.validator.ValidationResult
import es.weso.rdf.{RDFReader, RDFTriples}
import es.weso.rdfgraph.nodes.{RDFNode, IRI}
import es.weso.shacl.{Schema => ShaclSchema, ShaclResult, ShaclMatcher, SchemaFormats}
import es.weso.shex.{ShExMatcher, Schema => ShexSchema}
import play.Play
import play.api.Logger
import views.ResourceSerialiser

import scala.util.{Failure, Success}
import scalaz.Alpha.L

object ShapeMatcher {

  def matchWithShex(resource: RDFReader, resourceUri: String) = {
//    val shapeFile = Play.application().getFile("public/shapes/issue-simple.shex")
//    val strShape = Files.toString(shapeFile, Charsets.UTF_8)
    val schemaFormat = SchemaFormats.default
    val shexSchema = ShexSchema.fromFile("public/shapes/issue-simple.shex", schemaFormat).get._1
    val shapeUri = resourceUri + "Shape"

    //    Logger.debug("Schema: " + schema.toString())

    //    val strRDF = asString(graph, resource).get

    //    Logger.debug("Rdf Str: " + strRDF)

    val shExMatcher = ShExMatcher(shexSchema, resource)

    val shexResult = shExMatcher.matchAllIRIs_AllLabels()
    Logger.debug("Shex result: " + shexResult.isValid)
    shexResult.run match {
      case Success(rs) => {
//        Logger.debug("Shapes head: " + rs.head)

        val typings = shexResult.toList
        val typing = typings.head
        val shape = typing.map.get(IRI("http://example.org/Bob")).get.head

        Logger.debug("Typing: " + shape)

        for ((node, ts) <- typing.map) {
          Logger.debug("Node shape: " + nodeToString(node) + " " + nodesToString(ts))
        } // TODO HELP https://github.com/labra/rdfshape/blob/dae2ea4fa2adfc4eca758d7a2b52c7ebd6e0658d/app/controllers/ValidationResult.scala

        Logger.debug("Shape typings: " + typings)
      }
      case Failure(msg) => Logger.debug("The resource did not match any shape: " + msg)
    }

    //    Logger.debug("Shape single: " + result.toSingle)
    //    Logger.debug("Shape run: " + result.run)
    //    Logger.debug("Shape run: " + result.run.get)
    //    Logger.debug("Shape list:" + result.toList.toString)

    Logger.debug("MATCHES SHEX: " + shexResult.isValid)
    shexResult
  }

  def matchWithShacl(rdf: RDFReader) = {
    val schemaFormat = "TURTLE"
    Logger.debug("Available formats: " + SchemaFormats.toString)
    val shaclSchema = ShaclSchema.fromFile("public/shapes/issue-simple.shc", schemaFormat).get._1

    val shaclMatcher = ShaclMatcher(shaclSchema, rdf)

    val shaclResult = shaclMatcher.matchAllNodes_AllLabels

    Logger.debug("Result: " + shaclResult.toString)

    Logger.debug("MATCHES SHACL: " + shaclResult.isValid)

    shaclResult.isValid
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