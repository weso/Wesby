package views

import models.QueryEngineDependencies
import org.w3.banana.io.{JsonLdExpanded, JsonLdFlattened, RDFWriter}
import org.w3.banana.jena.{Jena, JenaModule}
import org.w3.banana.{JsonLDWriterModule, NTriplesWriterModule, RDFXMLWriterModule, TurtleWriterModule}
import play.Logger

import scala.util.Try


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
    jsonldCompactedWriter.asString(graph, base)
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

