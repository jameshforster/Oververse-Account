package models

/**
  * Created by james-forster on 22/05/17.
  */
sealed trait Response
sealed trait ErrorResponse extends Response {
  val message: String
}

case class SuccessResponse[T] (content: T) extends Response
case class NoContentResponse() extends Response

class BadRequestException(message: String) extends Exception
class ConflictException(message: String) extends Exception
class BadGatewayException(message: String) extends Exception