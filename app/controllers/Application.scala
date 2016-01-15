package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException

object Application extends Controller {

  def foodtrucks = Action {
    try {
      val fut: Future[List[Truck]] = Future { Truck.listAll() }
      val response = Await.result(fut, 2500 milliseconds).asInstanceOf[List[Truck]]
      Ok(response.mkString("\n"))
    } catch {
      case e: TimeoutException => InternalServerError("Timeout exceeded")
      case e: Exception => InternalServerError("Unknown server error occured")
    }
  }

}
