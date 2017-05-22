package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Overlord59 on 18/05/2017.
  */
case class EncryptedUser(username: String,
                         password: EncryptedString,
                         email: EncryptedString,
                         level: Int = 0,
                         token: Option[EncryptedToken] = None)

object EncryptedUser {
  implicit val JsonFormats: OFormat[EncryptedUser] = Json.format[EncryptedUser]

  implicit val encrypt: User => EncryptedUser =
    user =>
      EncryptedUser(
        user.username,
        user.password,
        user.email,
        user.level,
        user.token.map{_})
}