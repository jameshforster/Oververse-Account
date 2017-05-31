package controllers

import java.time.LocalDateTime

import helpers.TestSpec
import models.{EncryptedString, EncryptedToken, Token}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{Json, OFormat}
import play.api.test.FakeRequest
import services.AuthService

import scala.concurrent.Future

/**
  * Created by james-forster on 24/05/17.
  */
class AuthControllerSpec extends TestSpec {

  def setupController(serviceResponse: Future[Token]): AuthController = {
    val mockService = mock[AuthService]

    when(mockService.login(any(), any()))
      .thenReturn(serviceResponse)

    when(mockService.authorise(any(), any(), any()))
      .thenReturn(serviceResponse)

    new AuthController(mockService)
  }

  "Calling .login" when {

    "provided with no body" should {
      lazy val controller = setupController(Future.successful(Token("token")))
      lazy val result = controller.login("name")(FakeRequest("POST", ""))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return the message for a bad request" in {
        bodyOf(result) shouldBe "\"Invalid submission body: None for login\""
      }
    }

    "provided with a valid body" should {
      val tokenVal = Token("token", LocalDateTime.of(2016, 5, 1, 10, 10))
      lazy val controller = setupController(Future.successful(tokenVal))
      lazy val result = controller.login("name")(FakeRequest("POST", "").withJsonBody(Json.toJson("password")))

      "return a status of 200" in {
        statusOf(result) shouldBe 200
      }

      "return a token" in {
        bodyOf(result) shouldBe "{\"token\":\"token\",\"expiration\":\"2016-05-01T10:10:00\"}"
      }
    }

    "an error occurs which is handled by the exception handler" should {
      lazy val controller = setupController(Future.failed(new Exception("Error message")))
      lazy val result = controller.login("name")(FakeRequest("POST", "").withJsonBody(Json.toJson("password")))

      "return the correct status" in {
        statusOf(result) shouldBe 500
      }

      "return the correct message" in {
        bodyOf(result) shouldBe "\"Error message\""
      }
    }
  }

  "Calling .authorise" when {

    "provided with no body" should {
      lazy val controller = setupController(Future.successful(Token("token")))
      lazy val result = controller.authorise("name", 5)(FakeRequest("POST", ""))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return the message for a bad request" in {
        bodyOf(result) shouldBe "\"Invalid submission body: None for authorisation\""
      }
    }

    "provided with a valid body" should {
      val tokenVal = Token("token", LocalDateTime.of(2016, 5, 1, 10, 10))
      lazy val controller = setupController(Future.successful(tokenVal))
      lazy val result = controller.authorise("name", 5)(FakeRequest("POST", "").withJsonBody(Json.toJson(Token("name"))))

      "return a status of 200" in {
        statusOf(result) shouldBe 200
      }

      "return a token" in {
        bodyOf(result) shouldBe "{\"token\":\"token\",\"expiration\":\"2016-05-01T10:10:00\"}"
      }
    }

    "an error occurs which is handled by the exception handler" should {
      lazy val controller = setupController(Future.failed(new Exception("Error message")))
      lazy val result = controller.authorise("name", 5)(FakeRequest("POST", "").withJsonBody(Json.toJson(Token("name"))))

      "return the correct status" in {
        statusOf(result) shouldBe 500
      }

      "return the correct message" in {
        bodyOf(result) shouldBe "\"Error message\""
      }
    }
  }
}
