package services

import java.time.LocalDateTime

import connectors.MongoConnector
import helpers.TestSpec
import models._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._

import scala.concurrent.Future

/**
  * Created by james-forster on 22/05/17.
  */
class AuthServiceSpec extends TestSpec {

  def setupService(fetchResponse: Future[Option[EncryptedUser]], saveResponse: Future[Unit]): AuthService = {
    val mockConnector = mock[MongoConnector]

    when(mockConnector.getEntry[EncryptedUser](any(), any(), any())(any()))
      .thenReturn(fetchResponse)

    when(mockConnector.putEntry(any(), any())(any()))
      .thenReturn(saveResponse)

    when(mockConnector.updateEntry(any(), any(), any(), any())(any()))
      .thenReturn(saveResponse)

    new AuthService(mockConnector)
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

    "return a Created Response when the user is registered" in {
      lazy val service = setupService(Future.successful(None), Future.successful {})
      lazy val result = service.register(User("name", "P4ssword", "email@example.com"))

      await(result).isInstanceOf[CreatedResponse] shouldBe true
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

    "return a token when user details are validated" in {
      lazy val service = setupService(Future.successful(Some(User("name", "P4ssword", "example@email.com"))), Future.successful {})
      lazy val result = service.login("name", "P4ssword")

      await(result).isInstanceOf[Token] shouldBe true
    }
  }

  "Calling .authorise" should {

    "return a BadGatewayException when the mongo connection fails on request" which {
      lazy val service = setupService(Future.failed(new BadGatewayException("Failed to read database")), Future.successful {})
      lazy val result = service.authorise("name", Token("token"), 0)

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to read database"
      }
    }

    "return a BadRequestException when no matching user is found" which {
      lazy val service = setupService(Future.successful(None), Future.successful {})
      lazy val result = service.authorise("name", Token("token"), 0)

      lazy val exception = intercept[BadRequestException] { await(result) }

      "has the BadRequestException message" in {
        exception.getMessage shouldBe "User with name name does not exist."
      }
    }

    "return an ForbiddenException when the user has insufficient permissions" which {
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "email@example.com", 1))), Future.successful {})
      lazy val result = service.authorise("name", Token("token"), 5)

      lazy val exception = intercept[ForbiddenException] { await(result) }

      "has the ForbiddenException message" in {
        exception.getMessage shouldBe "The auth level 1 for user name is below the required value of 5"
      }
    }

    "return an UnauthorisedException when the user has no token" which {
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "email@example.com", 1))), Future.successful {})
      lazy val result = service.authorise("name", Token("token"), 1)

      lazy val exception = intercept[UnauthorisedException] { await(result) }

      "has the UnauthorisedException message" in {
        exception.getMessage shouldBe "No token found for user name"
      }
    }

    "return an UnauthorisedException when the token has expired" which {
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "email@example.com", 4,
        Some(Token("token", LocalDateTime.now().minusMinutes(1)))))), Future.successful {})
      lazy val result = service.authorise("name", Token("token"), 1)

      lazy val exception = intercept[UnauthorisedException] { await(result) }

      "has the UnauthorisedException message" in {
        exception.getMessage shouldBe "Auth token for name has expired"
      }
    }

    "return an UnauthorisedException when the tokens do not match" which {
      val time = LocalDateTime.now().plusMinutes(30)
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "email@example.com", 4,
        Some(Token("token1", time))))), Future.successful {})
      lazy val result = service.authorise("name", Token("token2", time), 4)

      lazy val exception = intercept[UnauthorisedException] { await(result) }

      "has the UnauthorisedException message" in {
        exception.getMessage shouldBe "Invalid token for name"
      }
    }

    "return a BadGatewayException when the mongo connection fails on save" which {
      val token = Token("token")
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "example@email.com", 4,
        Some(token)))), Future.failed(new BadGatewayException("Failed to write to database")))
      lazy val result = service.authorise("name", token, 4)

      lazy val exception = intercept[BadGatewayException] { await(result) }

      "has the BadGateway message" in {
        exception.getMessage shouldBe "Failed to write to database"
      }
    }

    "return a token when user details are validated" in {
      val token = Token("token")
      lazy val service = setupService(Future.successful(Some(EncryptedUser("name", "P4ssword", "example@email.com", 4,
        Some(token)))), Future.successful {})
      lazy val result = service.authorise("name", token, 4)

      await(result).isInstanceOf[Token] shouldBe true
    }
  }
}
