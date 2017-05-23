package controllers

import models.{BadGatewayException, BadRequestException, ConflictException}
import play.api.libs.json.Json
import play.api.mvc.{Controller, Result}

import scala.concurrent.Future

/**
  * Created by Overlord59 on 22/05/2017.
  */
trait OververseController extends Controller {

  val handleError: PartialFunction[Throwable, Future[Result]] = {
    case e: BadRequestException => Future.successful(BadRequest(Json.toJson(e.getMessage)))
    case e: ConflictException => Future.successful(Conflict(Json.toJson(e.getMessage)))
    case e: BadGatewayException => Future.successful(BadGateway(Json.toJson(e.getMessage)))
    case e => Future.successful(InternalServerError(Json.toJson(e.getMessage)))
  }
}
