package services

import com.google.inject.{Inject, Singleton}
import connectors.MongoConnector
import models._
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by james-forster on 22/05/17.
  */
@Singleton
class AccountService @Inject()(mongoConnector: MongoConnector) {

  def register(user: User): Future[Response] = {

    def saveNewUser(user: EncryptedUser): Future[Response] = {
      mongoConnector.putEntry("accounts", user).map { _ =>
        NoContentResponse()
      }
    }

    mongoConnector.getEntry[EncryptedUser]("accounts", "username", Json.toJson(user.username)).flatMap {
      case Some(_) => throw new ConflictException(s"User with name ${user.username} already exists.")
      case _ => saveNewUser(user)
    }
  }
}
