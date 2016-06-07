package views

import java.io.ByteArrayOutputStream
import java.util

import com.github.jsonldjava.core.JsonLdProcessor
import com.github.jsonldjava.utils.JsonUtils
import com.hp.hpl.jena.graph.Graph
import models.{QueryEngineDependencies, Resource}
import org.apache.jena.riot.{RDFDataMgr, RDFFormat, RDFWriterRegistry}
import org.apache.jena.riot.out.JsonLDWriter
import org.w3.banana.io.{JsonLdExpanded, JsonLdFlattened, RDFWriter}
import org.w3.banana.jena.{Jena, JenaModule}
import org.w3.banana.{JsonLDWriterModule, NTriplesWriterModule, RDFXMLWriterModule, TurtleWriterModule}
import play.api.Logger
//import org.json4s._
//import org.json4s.JsonDSL
//import org.json4s.native.JsonParser
//import org.json4s.native.JsonMethods
//import org.json4s.native.Printer
//import org.json4s.JsonAST
//import org.json4s.native.Serialization.write

import scala.util.{Success, Try}


/**
 * Created by jorge on 15/7/15.
 */
trait ResourceSerialiserTrait
  extends QueryEngineDependencies
  with TurtleWriterModule
  with NTriplesWriterModule
  with JsonLDWriterModule
  with RDFXMLWriterModule {

  import ops._
  import sparqlOps._

  def solutionsAsPlainText(solutions: Rdf#Solutions, header: String) = {
    val sb = new StringBuilder

    sb.append(header)
    sb.append("\n\n")

    val result = for (row <- solutions.iterator) yield {
      row("r1").get.as[Rdf#URI].get + "\t" + row("r2").get.as[Rdf#Node].get
    }

    sb.append(result.mkString("\n"))
    sb.mkString
  }

  def asPlainText(graph: Rdf#Graph, header: String): String = {
    val sb = new StringBuilder

    sb.append(header)
    sb.append("\n\n")

    val result = graph.triples.collect {
      case Triple(URI(s), p, o) => s"$p\t$o"
    } // collect should drop the failed items

    sb.append(result.mkString("\n"))
    sb.mkString
  }

  def asTurtle(graph: Rdf#Graph, base: String): Try[String] = {
    turtleWriter.asString(graph, "") // TODO base?
  }

  def asNTriples(graph: Rdf#Graph, base: String): Try[String] = {
    ntriplesWriter.asString(graph, base)
  }

  def asJsonLd(graph: Rdf#Graph, base: String): Try[String] = {
//    val out = new ByteArrayOutputStream()
//    RDFDataMgr.write(out, graph.asInstanceOf[Graph], RDFFormat.JSONLD_PRETTY)
//    Logger.debug(out.toString("UTF-8"))

    jsonldFlattenedWriter.asString(graph, base)
  }

  def asTemplateData(resource: Resource[Jena]): Try[String] = {
//    implicit val formats = DefaultFormats

//    val json = (
//      "@context" -> "context",
//      "@id" -> "id",
//      "@type" -> "type"
//      )



    Success("")
  }

  def asN3(graph: Rdf#Graph, base: String): Try[String] = ???

  def asRdfXml(graph: Rdf#Graph, base: String): Try[String] = {
    rdfXMLWriter.asString(graph, base)
  }

}

object ResourceSerialiser extends ResourceSerialiserTrait with JenaModule {
  // From github.com/pixelhumain/cityData/cityData_server_scala/app/models/SPARQLDatabaseJena.scala
  override implicit val jsonldExpandedWriter: RDFWriter[Jena, Try, JsonLdExpanded] =
    jsonldCompactedWriter.asInstanceOf[RDFWriter[Rdf, Try, JsonLdExpanded]]
  override implicit val jsonldFlattenedWriter: RDFWriter[Jena, Try, JsonLdFlattened] =
    jsonldCompactedWriter.asInstanceOf[RDFWriter[Rdf, Try, JsonLdFlattened]]
}

