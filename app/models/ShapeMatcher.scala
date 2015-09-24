package models

import com.google.common.base.Charsets
import com.google.common.io.Files
import es.weso.monads.Result
import es.weso.rdf.validator.ValidationResult
import es.weso.rdf.{PrefixMap, RDFReader, RDFTriples}
import es.weso.rdfgraph.nodes.{RDFNode, IRI}
import es.weso.shacl.{Schema => ShaclSchema, ShaclResult, ShaclMatcher, SchemaFormats}
import es.weso.shex.{Schema => ShexSchema, Typing, ShExMatcher}
import play.Play
import play.api.Logger
import views.ResourceSerialiser

import scala.util.{Try, Failure, Success}
import scalaz.Alpha.L

object ShapeMatcher {



  def matchWithShex(rdf: RDFReader, resourceUri: String): Option[String] = {
//    val shapeFile = Play.application().getFile("public/shapes/issue-simple.shex")
//    val strShape = Files.toString(shapeFile, Charsets.UTF_8)
    val schemaFormat = SchemaFormats.default
    val shexSchema: Try[(ShexSchema, PrefixMap)] = ShexSchema.fromFile("public/shapes/issue-simple.shex", schemaFormat)

    val result = for (
      (schema, pm) <- shexSchema
    ) yield {
        val validator = ShExMatcher(shexSchema.get._1, rdf)
        val r = validator.matchAllIRIs_AllLabels()
        (r, pm)
      }

    result match {
      case Success((validationResult: Result[Typing], pm)) => {
        val typings: Stream[Typing] = validationResult.run.get
        val firstTyping: Typing = typings.head
        val shape: String = firstTyping.showTyping(pm)
        Option(shape)
      }
      case Failure(f) => {
        Logger.debug("Matching failed: " + f)
        None
      }
    }
  }

  def matchWithShacl(rdf: RDFReader) = {
    val schemaFormat = "TURTLE"
    Logger.debug("Available formats: " + SchemaFormats.toString)
    val shaclSchema: Try[(ShaclSchema, PrefixMap)] = ShaclSchema.fromFile("public/shapes/issue-simple.shc", schemaFormat)

    val result = for (
      (schema, pm) <- shaclSchema
    ) yield {
        val validator = ShaclMatcher(shaclSchema.get._1, rdf)
        val r = validator.matchAllNodes_AllLabels
        (r, pm)
      }

    result match {
      case Success((validationResult, pm)) => {
        Logger.debug("Shacl matching succeeded: " + validationResult)
      }
      case Failure(f) => Logger.debug("Shacl matching failed: " + f)
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