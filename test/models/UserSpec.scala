package models

import helpers.TestSpec

/**
  * Created by james-forster on 23/05/17.
  */
class UserSpec extends TestSpec {

  "Creating a User model" should {

    "return an invalid password exception" when {

      "provided with a password with no numbers" in {
        lazy val exception = intercept[Exception] {
          User("name", "Password", "email@example.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid password used."
      }

      "provided with a password with no capitals" in {
        lazy val exception = intercept[Exception] {
          User("name", "p4ssword", "email@example.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid password used."
      }

      "provided with a password with no lowercase letters" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4SSWORD", "email@example.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid password used."
      }

      "provided with a password with a size less than 8" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4sswor", "email@example.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid password used."
      }
    }

    "return an invalid email exception" when {

      "provided with an email without leading text" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4ssword", "@example.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid email used."
      }

      "provided with an email without an @" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4ssword", "emailexample.com")
        }

        exception.getMessage shouldBe "requirement failed: Invalid email used."
      }

      "provided with an email without a domain address" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4ssword", "email@")
        }

        exception.getMessage shouldBe "requirement failed: Invalid email used."
      }

      "provided with an email without a domain ending" in {
        lazy val exception = intercept[Exception] {
          User("name", "P4ssword", "email@example")
        }

        exception.getMessage shouldBe "requirement failed: Invalid email used."
      }
    }
  }
}
