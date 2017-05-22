package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by james-forster on 22/05/17.
  */
case class User(username: String,
                password: String,
                email: String,
                level: Int = 0,
                token: Option[Token] = None)

object User {
  implicit val jsonFormats: OFormat[User] = Json.format[User]

  implicit val decrypt: EncryptedUser => User =
    user =>
      User(
        user.username,
        user.password,
        user.email,
        user.level,
        user.token.map{_})
}
