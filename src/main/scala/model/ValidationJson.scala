package model

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import util.JsonUtils

import scala.util.{Failure, Success, Try}

case class ValidationJson(json: JsonNode, schema: JsonNode) {

  private val factory = JsonSchemaFactory.byDefault()

  private val jsonWithNullsRemoved = JsonUtils.stripNullValues(json)

  def isValid: Try[Unit] = {
    Try {
      val jsonSchema: JsonSchema = factory.getJsonSchema(schema)
      jsonSchema.validate(jsonWithNullsRemoved)
    }
      .flatMap { processingReport =>
        if (processingReport.isSuccess) Success(())
        else Failure(new Exception(processingReport.toString))
      }
  }
}
