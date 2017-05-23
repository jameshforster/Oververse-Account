package services

import connectors.MongoConnector
import helpers.TestSpec
import models._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._

import scala.concurrent.Future

/**
  * Created by james-forster on 22/05/17.
  */
class UserServiceSpec extends TestSpec {

  def setupService(fetchResponse: Future[Option[EncryptedUser]], saveResponse: Future[Unit]): UserService = {
    val mockConnector = mock[MongoConnector]

    when(mockConnector.getEntry[EncryptedUser](any(), any(), any())(any()))
      .thenReturn(fetchResponse)

    when(mockConnector.putEntry(any(), any())(any()))
      .thenReturn(saveResponse)

    when(mockConnector.updateEntry(any(), any(), any(), any())(any()))
      .thenReturn(saveResponse)

    new UserService(mockConnector)
  }

  "Calling .register" should {

    "return a BadGatewayException when the mongo connection fails on request" which {
      lazy val service = setupService(Future.failed(new BadGatewayException("Failed to read database")), Future.successful {})
      lazy val result = service.register(User("name", "P4ssword", "email@example.com"))

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to read database"
      }
    }

    "return a ConflictException when a duplicate user is found" which {
      lazy val service = setupService(Future.successful(Some(User("name", "P4ssword", "email@example.com"))), Future.successful {})
      lazy val result = service.register(User("name", "P4ssword", "email@example.com"))

      lazy val exception = intercept[ConflictException] { await(result) }

      "has the conflict message for a user" in {
        exception.getMessage shouldBe "User with name name already exists."
      }
    }

    "return a BadGatewayException when the mongo connection fails on put" which {
      lazy val service = setupService(Future.successful(None), Future.failed(new BadGatewayException("Failed to write to database")))
      lazy val result = service.register(User("name", "P4ssword", "email@example.com"))

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to write to database"
      }
    }

    "return a NoContent Response when the user is registered" in {
      lazy val service = setupService(Future.successful(None), Future.successful {})
      lazy val result = service.register(User("name", "P4ssword", "email@example.com"))

      await(result).isInstanceOf[NoContentResponse] shouldBe true
    }
  }

  "Calling .login" should {

    "return a BadGatewayException when the mongo connection fails on request" which {
      lazy val service = setupService(Future.failed(new BadGatewayException("Failed to read database")), Future.successful {})
      lazy val result = service.login("name", "P4ssword")

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to read database"
      }
    }

    "return a BadRequestException when no matching user is found" which {
      lazy val service = setupService(Future.successful(None), Future.successful {})
      lazy val result = service.login("name", "P4ssword")

      lazy val exception = intercept[BadRequestException] { await(result) }

      "has the BadRequest message for a missing user" in {
        exception.getMessage shouldBe "User with name name does not exist."
      }
    }

    "return a BadRequestException when an incorrect password is supplied" which {
      lazy val service = setupService(Future.successful(Some(User("name", "D1fferent", "example@email.com"))), Future.successful {})
      lazy val result = service.login("name", "P4ssword")

      lazy val exception = intercept[BadRequestException] { await(result) }

      "has the BadRequest message for an incorrect password" in {
        exception.getMessage shouldBe "Incorrect password for user name."
      }
    }

    "return a BadGatewayException when the mongo connection fails on save" which {
      lazy val service = setupService(Future.successful(Some(User("name", "P4ssword", "example@email.com"))), Future.failed(new BadGatewayException("Failed to write to database")))
      lazy val result = service.login("name", "P4ssword")

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to write to database"
      }
    }

    "return an encrypted token when user details are validated" in {
      lazy val service = setupService(Future.successful(Some(User("name", "P4ssword", "example@email.com"))), Future.successful {})
      lazy val result = service.login("name", "P4ssword")

      await(result).isInstanceOf[EncryptedToken]
    }
  }
}
