package controllers

import com.google.inject.{Inject, Singleton}
import models.{BadRequestException, User}
import play.api.mvc.{Action, AnyContent}
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

/**
  * Created by Overlord59 on 22/05/2017.
  */
@Singleton
class AccountController @Inject()(accountService: AuthService) extends OververseController {

  val register: Action[AnyContent] = Action.async {
    implicit request => {
      Try{request.body.asJson.get.as[User]} match {
        case Success(data) => accountService.register(data).map { _ => Created }
        case _ => Future.failed(new BadRequestException(s"Invalid submission body: ${request.body.asJson} for type User"))
      }
    }.recoverWith(handleError)
  }
}
