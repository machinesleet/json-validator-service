package jsonValidator.util

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader

import java.io.{BufferedWriter, File, FileWriter}
import scala.concurrent.{ExecutionContext, Future}

object SchemaIO {

  def pathWithSchemaIdAndFileExt(schemaFilePath: String, schemaId: String) = s"$schemaFilePath/$schemaId.json"

  /**
   * Loads a JsonNode from the schema file path with schema id
   *
   * @param schemaFilePath the 'base' schema file path
   * @param schemaId       the schema id
   */
  def loadJsonNode(schemaFilePath: String, schemaId: String)(implicit ec: ExecutionContext): Future[JsonNode] = {
    Future {
      val path = pathWithSchemaIdAndFileExt(schemaFilePath, schemaId)
      JsonLoader.fromPath(path)
    }
  }

  /**
   * @param schemaFilePath the 'base' file path to write the json schema to
   * @param schemaId       schema id to be used as the filename
   * @param jsonString     json string to write at the file path
   */
  def writeSchema(schemaFilePath: String,
                  schemaId: String,
                  jsonString: String)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      val pathWithSchemaId = pathWithSchemaIdAndFileExt(schemaFilePath, schemaId)

      val file = new File(pathWithSchemaId)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(jsonString)
      bw.close()
    }
  }

}
