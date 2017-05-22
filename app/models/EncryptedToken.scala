package models

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

/**
  * Created by james-forster on 22/05/17.
  */
case class EncryptedToken(token: EncryptedString, expiration: LocalDateTime = LocalDateTime.now().plusMinutes(30))

object EncryptedToken {
  implicit val jsonFormats: OFormat[EncryptedToken] = Json.format[EncryptedToken]

  implicit val encrypt: Token => EncryptedToken =
    token =>
      EncryptedToken(token.token, token.expiration)
}
