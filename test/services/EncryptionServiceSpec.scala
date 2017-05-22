package services

import config.ApplicationConfig
import helpers.TestSpec
import org.scalatestplus.play.OneAppPerSuite

/**
  * Created by james-forster on 04/04/17.
  */
class EncryptionServiceSpec extends TestSpec with OneAppPerSuite {

  lazy val config: ApplicationConfig = app.injector.instanceOf[ApplicationConfig]
  lazy val service = new EncryptionService(config)

  "Calling .encrypt" should {
    lazy val result = service.encrypt("testData")

    "return a map" which {

      "contains a nonce of size 64" in {
        result("nonce").length shouldBe 64
      }

      "contains a value of size 32" in {
        result("value").length shouldBe 32
      }
    }
  }

  "Calling .decrypt" should {
    val map = Map(
      "nonce" -> "a954bf74662060335285a4b482055ef8b9b38eeee1808f97ea7602fcde77b2ed",
      "value" -> "33b8c73001f82ca28f3e26e1af1db245"
    )
    lazy val result = service.decrypt(map)

    "return a string of 'testData'" in {
      result shouldBe "testData"
    }
  }
}
