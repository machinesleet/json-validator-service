package util

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}

import scala.util.Try

object JsonUtils {

  // not suitable for large nested files as not tail recursive
  def stripNullValues(node: JsonNode): JsonNode = {
    val nodeElems = node.elements()

    while (nodeElems.hasNext) {
      val node = nodeElems.next()

      if (node.isObject) stripNullValues(node)
      else if (node.isNull) nodeElems.remove()
    }

    node
  }

  def parseJsonSchema(schemaString: String) : Try[JsonSchema] = {
    Try {
      val jsonNode: JsonNode = JsonLoader.fromString(schemaString)
      val factory = JsonSchemaFactory.byDefault()
      factory.getJsonSchema(jsonNode)
    }
  }

}
