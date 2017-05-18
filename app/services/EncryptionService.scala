package services

import java.security.MessageDigest
import java.util
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import com.google.inject.{Inject, Singleton}
import config.ApplicationConfig
import org.apache.commons.codec.binary.Hex

/**
  * Created by james-forster on 04/04/17.
  */

@Singleton
class EncryptionService @Inject()(applicationConfig: ApplicationConfig) {

  private val sha = MessageDigest.getInstance("SHA-1")
  private val secretKey: SecretKeySpec = new SecretKeySpec(util.Arrays.copyOf(sha.digest(applicationConfig.encryptionKey.getBytes()), 16), "AES")
  private val cipher = Cipher.getInstance("AES")
  private val encoder = new Hex

  def encrypt(input: String): Map[String, String] = {

    def kestrel[A](x: A)(f: A => Unit): A = {
      f(x)
      x
    }

    val random = new Random()
    val nonce = encoder.encode(kestrel(Array.fill[Byte](8)(0))(random.nextBytes))
    val encryptedNonce = {
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      cipher.doFinal(nonce)
    }
    val nonceKey = new SecretKeySpec(nonce, "AES")
    val encryptedResult = {
      cipher.init(Cipher.ENCRYPT_MODE, nonceKey)
      cipher.doFinal(input.getBytes)
    }

    Map(
      "nonce" -> new String(encoder.encode(encryptedNonce)),
      "value" -> new String(encoder.encode(encryptedResult))
    )
  }

  def decrypt(data: Map[String, String]): String = {
    val nonce = encoder.decode(data("nonce").getBytes())
    val decryptedNonce = {
      cipher.init(Cipher.DECRYPT_MODE, secretKey)
      cipher.doFinal(nonce)
    }
    val nonceKey = new SecretKeySpec(decryptedNonce, "AES")
    val result = encoder.decode(data("value").getBytes())
    val decryptedData = {
      cipher.init(Cipher.DECRYPT_MODE, nonceKey)
      cipher.doFinal(result)
    }

    new String(decryptedData)
  }
}
