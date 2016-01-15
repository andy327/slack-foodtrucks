package controllers

import scala.collection.JavaConverters._
import com.fasterxml.jackson.databind.{ ObjectMapper, JsonNode }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import play.api.Play
import sys.process._

case class Truck (
  name: String,
  address: String,
  cuisine: List[String],
  url: String) {
  override def toString: String = s"""$name (${cuisine.mkString(",")}) @ $address [$url]"""
}

object Truck {
  def newObjectMapper: ObjectMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
  }

  private[Truck] lazy val mapper = newObjectMapper

  val defaultLat = Play.maybeApplication.flatMap(_.configuration.getDouble("foodtrucks.latitude")).getOrElse(0.0)
  val defaultLon = Play.maybeApplication.flatMap(_.configuration.getDouble("foodtrucks.longitude")).getOrElse(0.0)

  def apply(jsonNode: JsonNode): Truck = {
    val info = jsonNode.get("obj")

    Truck(
      info.get("name").asText,
      info.get("last_seen").get("address").asText,
      info.get("cuisine").elements.asScala.map(_.asText).toList,
      info.get("url").asText
    )
  }

  def listAll(lat: Double = defaultLat, lon: Double = defaultLon): List[Truck] = {
    val result = Seq("curl", "-XGET", s"http://nyctruckfood.com/api/trucks/search?q=$lat,$lon").!!
    mapper.readTree(result).elements.asScala.map(Truck(_)).toList
  }
}
