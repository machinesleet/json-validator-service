package jsonValidator.util

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}

import scala.util.Try

object JsonUtils {

  /**
   * Remove null values from a jsonNode
   *
   * @note not suitable for large nested json files as not tail recursive
   * @param node the JsonNode to remove null values from
   * @return the JsonNode with null values removed
   */
  def stripNullValues(node: JsonNode): JsonNode = {
    val nodeElems = node.elements()

    while (nodeElems.hasNext) {
      val node = nodeElems.next()

      if (node.isObject) stripNullValues(node)
      else if (node.isNull) nodeElems.remove()
    }

    node
  }

  /**
   * @param schemaString to be loaded in to a JsonNode then parsed as a schema by JsonSchemaFactory
   */
  def parseJsonSchema(schemaString: String): Try[JsonSchema] = {
    Try {
      val jsonNode: JsonNode = JsonLoader.fromString(schemaString)
      val factory = JsonSchemaFactory.byDefault()
      factory.getJsonSchema(jsonNode)
    }
  }

  /**
   * @param jsonString jsonString to construct a JsonNode from
   */
  def loadJsonNode(jsonString: String): Try[JsonNode] = {
    Try(JsonLoader.fromString(jsonString))
  }

}
