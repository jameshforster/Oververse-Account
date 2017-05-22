package controllers

import helpers.TestSpec
import models.{BadGatewayException, BadRequestException, ConflictException}

/**
  * Created by Overlord59 on 22/05/2017.
  */
class OververseControllerSpec extends TestSpec {

  "Calling .handleError" when {
    lazy val controller = new OververseController {

    }

    "passed a BadRequestException" should {
      lazy val result = controller.handleError(new BadRequestException("Bad request"))

      "return a status of 400" in {
        statusOf(result) shouldBe 400
      }

      "return a body containing the error message" in {
        bodyOf(result) shouldBe "\"Bad request\""
      }
    }

    "passed a ConflictException" should {
      lazy val result = controller.handleError(new ConflictException("Conflict"))

      "return a status of 409" in {
        statusOf(result) shouldBe 409
      }

      "return a body containing the error message" in {
        bodyOf(result) shouldBe "\"Conflict\""
      }
    }

    "passed a BadGatewayException" should {
      lazy val result = controller.handleError(new BadGatewayException("Bad gateway"))

      "return a status of 502" in {
        statusOf(result) shouldBe 502
      }

      "return a body containing the error message" in {
        bodyOf(result) shouldBe "\"Bad gateway\""
      }
    }

    "passed a generic exception" should {
      lazy val result = controller.handleError(new Exception("General error"))

      "return a status of 500" in {
        statusOf(result) shouldBe 500
      }

      "return a body containing the error message" in {
        bodyOf(result) shouldBe "\"General error\""
      }
    }
  }
}
