package util

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader

import scala.concurrent.{ExecutionContext, Future}

object SchemaLoader {

  val schemaResourcePath = "schemas"

  def loadSchema(schemaId: Int)(implicit ec: ExecutionContext): Future[JsonNode] = {
    Future {
      val pathWithSchemaIdAndFileExt= s"/$schemaResourcePath/$schemaId.json"
      JsonLoader.fromResource(pathWithSchemaIdAndFileExt)
    }
  }

}
