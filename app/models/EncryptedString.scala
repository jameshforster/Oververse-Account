package models

import play.api.Play
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService

/**
  * Created by Overlord59 on 18/05/2017.
  */

case class EncryptedString(eString: Map[String, String])

object EncryptedString {

  lazy val encryptionService: EncryptionService = Play.application.injector.instanceOf[EncryptionService]

  implicit val encryptString: String => EncryptedString =
    string =>
      EncryptedString(encryptionService.encrypt(string))

  implicit val decryptString: EncryptedString => String =
    data =>
      encryptionService.decrypt(data.eString)

  implicit val jsonFormats: OFormat[EncryptedString] = Json.format[EncryptedString]
}
