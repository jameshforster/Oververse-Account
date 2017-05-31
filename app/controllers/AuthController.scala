package controllers

import com.google.inject.{Inject, Singleton}
import models.{BadRequestException, Token}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.AuthService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by james-forster on 24/05/17.
  */
@Singleton
class AuthController @Inject()(authService: AuthService) extends OververseController {

  val login: String => Action[AnyContent] = username => Action.async {
    implicit request => {
      request.body.asJson.flatMap { _.asOpt[String] } match {
        case Some(password) => authService.login(username, password).map { token => Ok(Json.toJson(token)) }
        case _ => Future.failed(new BadRequestException(s"Invalid submission body: ${request.body.asJson} for login"))
      }
    }.recoverWith(handleError)
  }

  def authorise(username: String, level: Int): Action[AnyContent] = Action.async {
    implicit request => {
      request.body.asJson.flatMap { _.asOpt[Token]} match {
        case Some(token) => authService.authorise(username, token, level).map { result => Ok(Json.toJson(result))}
        case _ => Future.failed(new BadRequestException(s"Invalid submission body: ${request.body.asJson} for authorisation"))
      }
    }.recoverWith(handleError)
  }
}
