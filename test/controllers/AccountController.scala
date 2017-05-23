package controllers

import com.google.inject.{Inject, Singleton}
import models.{BadRequestException, User}
import play.api.mvc.{Action, AnyContent}
import services.AccountService

/**
  * Created by Overlord59 on 22/05/2017.
  */
@Singleton
class AccountController @Inject()(accountService: AccountService) extends OververseController {

  val register: Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap { _.asOpt[User] } match {
      case Some(data) => accountService.register(data).map { _ => NoContent }
      case _ => throw new BadRequestException("Invalid submission for type: User")
    }
  }
}
