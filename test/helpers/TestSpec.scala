package helpers

import akka.stream.{Attributes, ClosedShape, Graph, Materializer}
import akka.util.ByteString
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.mvc.Result

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by james-forster on 14/03/17.
  */
trait TestSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  def await[T](future: Future[T]): T = {
    Await.result(future, 5 seconds)
  }

  def statusOf(result: Future[Result]): Int = await(result).header.status

  def bodyOf(result: Future[Result]): String = await(consumeBody(result)).decodeString("utf-8")

  private def consumeBody(result: Future[Result])(implicit mat: Materializer = materializer): Future[ByteString] = {
    await(result).body.consumeData
  }

  private val materializer: Materializer = mock[Materializer]
}
