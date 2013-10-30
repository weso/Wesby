package controllers

import play.api._
import play.api.mvc._
import es.weso.wfLodPortal.ModelLoader
import es.weso.wfLodPortal.Prefixes
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

object Application extends Controller {

  val Html = Accepting("text/html")

  val PlainText = Accepting("text/plain")

  val N3 = Accepting("text/n3")

  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")

  val Xml = Accepting("application/xml")
  val RdfXML = Accepting("application/rdf+xml")

  val RdfJSON = Accepting("application/rdf+json")

  val charsetDecoder = Charset.forName("UTF-8").newDecoder();
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

  def index = Action {
    Redirect {
      routes.Application.fallback(ModelLoader.indexPath)
    }
  }

  def fallback(uri: String) = Action {
    implicit request =>
      val resultQuery = ModelLoader.loadUri(uri)
      val subjectModel = resultQuery.subject.jenaModel
      val predicateModel = resultQuery.predicate.jenaModel
      val models = List(subjectModel, predicateModel)
      render {
        case Html() => Ok(views.html.fallback(processURIs(resultQuery)))
        case N3() =>
          renderModels(models, ("N3", "utf-8", N3.mimeType))
        case Turtle() =>
          renderModels(models, ("TURTLE", "utf-8", Turtle.mimeType))
        case XTurtle() =>
          renderModels(models, ("TURTLE", "utf-8", XTurtle.mimeType))
        case RdfXML() =>
          renderModels(models, ("RDF/XML", "utf-8", RdfXML.mimeType))
        case Xml() =>
          renderModels(models, ("RDF/XML", "utf-8", Xml.mimeType))
        case RdfJSON() =>
          renderModels(models, ("RDF/JSON", "utf-8", RdfJSON.mimeType))
        case PlainText() =>
          renderModels(models, ("N-Triples", "utf-8", PlainText.mimeType))
      }
  }

  def renderModels(models: Seq[JenaModel], contentType: (String, String, String)) = {
    val out = new ByteArrayOutputStream()
    for (model <- models) {
      model.write(out, contentType._1, contentType._2)
    }

    Ok(out.toString).as {
      new StringBuilder(contentType._3).append(" ; charset=")
        .append(contentType._2).toString
    }
  }
  
  def processURIs(resultQuery: models.ResultQuery) : models.ResultQuery = {
  
  	for(n <- resultQuery.subject.list){
  		processNode(n._1)
		
		for(a <- n._2) {
			processNode(a._1)
		}
  	}
  	
	for(m <- resultQuery.predicate.list) {
		processNode(m._1)
		
		for(a <- m._2) {
			processNode(a._1)
		}
	}  	
  	
  	return resultQuery
  }
  
  def processNode(n: models.Node) : Unit  = {
	n match {
		case n:models.Resource => {
			n.label match {
				case Some(label) => { }
				case None => { n.label = processURI(n.uri) }
			}
		}
		case n:models.Property => {
			n.label match {
				case Some(label) => { }
				case None => { n.label = processURI(n.uri) }
			}
		}
		case n:models.Literal => { }
	}
  }
  
  def processURI(uri: String) : Option[String]  = {
  	val index = math.max(uri.lastIndexOf("#"), uri.lastIndexOf("/")) + 1
  	val prefix = uri.subSequence(0, index)
  	val ending = uri.substring(index)
  	
  	return Prefixes.replacePrefix(uri, prefix.toString, ending.toString)
  }

}