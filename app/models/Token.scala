package models

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Overlord59 on 18/05/2017.
  */
case class Token(token: String, expiration: LocalDateTime = LocalDateTime.now().plusMinutes(30))

object Token {
  implicit val jsonFormats: OFormat[Token] = Json.format[Token]

  implicit val decrypt: EncryptedToken => Token =
    token =>
      Token(token.token, token.expiration)
}