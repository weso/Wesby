package models

import es.weso.monads.Result
import es.weso.rdf.{PrefixMap, RDFReader}
import es.weso.rdfgraph.nodes.{IRI, RDFNode}
import es.weso.shacl.{Schema => ShaclSchema, Label, ShaclResult, SchemaFormats, ShaclMatcher}
import es.weso.shex.{Schema => ShexSchema, ShapeSyntax, ShExMatcher, Typing}
import es.weso.typing.PosNegTyping
import play.Play
import play.api.Logger

import scala.util.{Failure, Success, Try}

object ShapeMatcher {

  val schemaFormat = Play.application().configuration().getString("shapes.format")
  val shexSchema: Try[(ShexSchema, PrefixMap)] = ShexSchema.fromFile("public/shapes/test-shapes.shex", schemaFormat)

  def matchWithShex(rdf: RDFReader, node: String): List[String] = {
    val labels = shexSchema.get._1.getLabels()
    Logger.debug("LABELS: " + labels)
    val results = for (label <- labels) yield matchLabelWithShex(rdf, node, label)
    results.flatten
  }

  def matchLabelWithShex(rdf: RDFReader, node: String, label: ShapeSyntax.Label): Option[String] = {
//    val shapeFile = Play.application().getFile("public/shapes/issue-simple.shex")
//    val strShape = Files.toString(shapeFile, Charsets.UTF_8)

    val result = for (
      (schema, pm) <- shexSchema
    ) yield {
        val validator = ShExMatcher(schema, rdf)
        val r = validator.matchIRI_Label(IRI(node))(label)
//        val r = validator.matchIRI_AllLabels(IRI(node))
        (r, pm)
      }

    result match {
      case Success((validationResult: Result[Typing], pm)) => {
        val typings: Stream[Typing] = validationResult.run().get
        val firstTyping: Option[Typing] = typings.headOption

        firstTyping match {
          case Some(t) => Option(t.showTyping(pm))
          case None => None
        }

//        firstTyping match {
//          case Some(typing) => Option(typing.showTyping(pm))
//          case None => {
//            Logger.debug("Shex: empty stream")
//            Logger.debug("Node: " + node)
//            Logger.debug("RDF: " + rdf)
//            None
//          }
//        }
      }
      case Failure(f) => {
        Logger.debug("Matching failed: " + f)
        Logger.debug("Node: " + node)
        Logger.debug("RDF: " + rdf)
        None
      }
    }
  }

  def matchWithShacl(rdf: RDFReader, node: String) = {
    val schemaFormat = SchemaFormats.default
    Logger.debug("RDF: " + rdf)
    Logger.debug("Available formats: " + SchemaFormats.toString + ". Choosing: " + schemaFormat)
    val shaclSchema: Try[(ShaclSchema, PrefixMap)] = ShaclSchema.fromFile("public/shapes/student.shex", schemaFormat)

    val result = for (
      (schema, pm) <- shaclSchema
    ) yield {
        val validator = ShaclMatcher(schema, rdf)
        val r = validator.match_node_AllLabels(IRI(node))
//        val r2 = validator.match_node_label(IRI(node))(validator.mkLabel("http://example.org/Student"))
        Logger.debug("VALIDATOR: " + r)
        (r, pm)
      }

//    Logger.debug("RESULT: " + result)
//    None

    result match {
      case Success((validationResult: ShaclResult, pm)) => {
        // TODO: extract shape name
        val shape: String = validationResult.show(1)(pm)

        Option(shape)
      }
      case Failure(f) => {
        Logger.debug("Matching failed: " + f)
        Logger.debug("Node: " + node)
        Logger.debug("RDF: " + rdf)
        None
      }
    }
//    result match {
//      case Success((validationResult: Result[Typing], pm)) => {
//        val typings: Stream[Typing] = validationResult.run().get
//        val firstTyping: Option[Typing] = typings.headOption
//
//        firstTyping match {
//          case Some(typing) => Option(typing.showTyping(pm))
//          case None => {
//            Logger.debug("Shex: empty stream")
//            Logger.debug("Node: " + node)
//            Logger.debug("RDF: " + rdf)
//            None
//          }
//        }
//      }
//      case Failure(f) => {
//        Logger.debug("Matching failed: " + f)
//        Logger.debug("Node: " + node)
//        Logger.debug("RDF: " + rdf)
//        None
//      }
//    }
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