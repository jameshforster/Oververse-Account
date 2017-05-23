package models

import play.api.libs.json.{Json, OFormat}

import scala.util.matching.Regex

/**
  * Created by james-forster on 22/05/17.
  */
case class User(username: String,
                password: String,
                email: String) {

  require(User.passwordCheck(password), "Invalid password used.")
  require(User.emailCheck(email), "Invalid email used.")
}

object User {
  implicit val jsonFormats: OFormat[User] = Json.format[User]

  val passwordCheck: String => Boolean =
    password => {
      val strongRegex = new Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.{8,})")
      strongRegex.findAllIn(password).nonEmpty
    }

  val emailCheck: String => Boolean =
    email => {
      val emailRegex = new Regex("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
      emailRegex.findAllIn(email).nonEmpty
    }
}
