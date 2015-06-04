package controllers

import javax.inject.Inject

import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc._

class Application @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def welcome = Action { implicit request =>
    Ok(Messages("welcome.test"))
  }

}
