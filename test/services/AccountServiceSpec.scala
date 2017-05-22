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
class AccountServiceSpec extends TestSpec {

  def setupService(fetchResponse: Future[Option[EncryptedUser]], putResponse: Future[Unit]): AccountService = {
    val mockConnector = mock[MongoConnector]

    when(mockConnector.getEntry[EncryptedUser](any(), any(), any())(any()))
      .thenReturn(fetchResponse)

    when(mockConnector.putEntry(any(), any())(any()))
      .thenReturn(putResponse)

    new AccountService(mockConnector)
  }

  "Calling .register" should {

    "return a BadGatewayException when the mongo connection fails on request" in {
      lazy val service = setupService(Future.failed(new BadGatewayException("Failed to read database")), Future.successful {})
      lazy val result = service.register(User("name", "password", "email"))

      intercept[Exception] {
        await(result)
      }.isInstanceOf[BadGatewayException]
    }

    "return a ConflictException when a duplicate user is found" in {
      lazy val service = setupService(Future.successful(Some(User("name", "password", "email"))), Future.successful {})
      lazy val result = service.register(User("name", "password", "email"))

      intercept[Exception] {
        await(result)
      }.isInstanceOf[ConflictException]
    }

    "return a BadGatewayException when the mongo connection fails on put" in {
      lazy val service = setupService(Future.successful(None), Future.failed(new BadGatewayException("Failed to write to database")))
      lazy val result = service.register(User("name", "password", "email"))

      intercept[Exception] {
        await(result)
      }.isInstanceOf[BadGatewayException]
    }

    "return a NoContent Response when the user is registered" in {
      lazy val service = setupService(Future.successful(None), Future.successful {})
      lazy val result = service.register(User("name", "password", "email"))

      await(result).isInstanceOf[NoContentResponse]
    }
  }
}
