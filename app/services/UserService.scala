package services

import com.google.inject.{Inject, Singleton}
import connectors.MongoConnector
import models._
import org.apache.commons.codec.binary.Hex
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

/**
  * Created by james-forster on 22/05/17.
  */
@Singleton
class UserService @Inject()(mongoConnector: MongoConnector) {

  private def createToken: Future[Token] = {
    val encoder = new Hex
    val token = new String(encoder.encode(Random.nextString(8).getBytes()))
    Future.successful(new Token(token))
  }

  def register(user: User): Future[Response] = {

    def saveNewUser(user: EncryptedUser): Future[NoContentResponse] = {
      mongoConnector.putEntry("accounts", user).map { _ =>
        NoContentResponse()
      }
    }

    mongoConnector.getEntry[EncryptedUser]("accounts", "username", Json.toJson(user.username)).flatMap {
      case Some(_) => throw new ConflictException(s"User with name ${user.username} already exists.")
      case _ => saveNewUser(user)
    }
  }

  def login(username: String, password: String): Future[EncryptedToken] = {

    def verifyPassword(expected: String): Future[Token] = {
      if (password == expected) createToken
      else throw new BadRequestException(s"Incorrect password for user $username.")
    }

    mongoConnector.getEntry[EncryptedUser]("accounts", "username", Json.toJson(username)).flatMap {
      case Some(user) =>
        for {
          token <- verifyPassword(user.password)
          _ <- mongoConnector.updateEntry[EncryptedUser]("accounts", "username", Json.toJson(username), EncryptedUser(user.username, user.password, user.email, user.level, Some(token)))
        } yield token
      case _ => throw new BadRequestException(s"User with name $username does not exist.")
    }
  }
}
