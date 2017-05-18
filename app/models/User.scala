package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Overlord59 on 18/05/2017.
  */
case class User (username: String,
                 password: EncryptedString,
                 email: EncryptedString,
                 token: Token,
                 activated: Boolean)

object User {
  implicit val JsonFormats: OFormat[User] = Json.format[User]
}