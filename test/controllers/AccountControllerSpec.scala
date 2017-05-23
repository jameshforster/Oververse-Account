package controllers

import helpers.TestSpec
import models.{NoContentResponse, Response, User}
import services.UserService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.Future

/**
  * Created by james-forster on 23/05/17.
  */
class AccountControllerSpec extends TestSpec {

  def setupController(response: Future[Response]): AccountController = {
    val mockAccountService = mock[UserService]

    when(mockAccountService.register(any()))
    .thenReturn(response)

    new AccountController(mockAccountService)
  }

  "Calling .register" when {

    "provided with no body" should {
      lazy val controller = setupController(Future.successful(NoContentResponse()))
      lazy val result = controller.register(FakeRequest("POST", ""))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return the message for a bad request" in {
        bodyOf(result) shouldBe "\"Invalid submission body: None for type User\""
      }
    }

    "provided with an invalid body" should {
      lazy val controller = setupController(Future.successful(NoContentResponse()))
      lazy val result = controller.register(FakeRequest("POST", "").withJsonBody(Json.toJson("invalid")))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return the message for a bad request" in {
        bodyOf(result) shouldBe "\"Invalid submission body: Some(\\\"invalid\\\") for type User\""
      }
    }

    "provided with an invalid email or password" should {
      lazy val controller = setupController(Future.successful(NoContentResponse()))
      lazy val result = controller.register(FakeRequest("POST", "").withJsonBody(Json.obj("username" -> "name", "password" -> "password", "email" -> "email")))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return the message for a bad request" in {
        bodyOf(result) shouldBe """"Invalid submission body: Some({\"username\":\"name\",\"password\":\"password\",\"email\":\"email\"}) for type User""""
      }
    }

    "provided with a valid user" should {
      lazy val controller = setupController(Future.successful(NoContentResponse()))
      lazy val result = controller.register(FakeRequest("POST", "").withJsonBody(Json.toJson(User("name", "P4ssword", "email@example.com"))))

      "return a status of 204" in {
        statusOf(result) shouldBe 204
      }
    }

    "an error occurs that is handled by the error handler" should {
      lazy val controller = setupController(Future.failed(new Exception("Exception message")))
      lazy val result = controller.register(FakeRequest("POST", "").withJsonBody(Json.toJson(User("name", "P4ssword", "email@example.com"))))

      "return the correct status" in {
        statusOf(result) shouldBe 500
      }

      "return the correct message" in {
        bodyOf(result) shouldBe "\"Exception message\""
      }
    }
  }
}
