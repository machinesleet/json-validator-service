import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.Materializer
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.JsonSchema
import model.{ActionResponse, ValidationJson}
import model.ActionResponse._
import model.ActionResponseProtocol._
import spray.json.enrichAny
import util.{JsonUtils, SchemaIO}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class Routes(schemaPath: String)(implicit mat: Materializer, e: ExecutionContextExecutor) {

  private val schemaPrefix = "schema"
  private val validatePrefix = "validate"

  val topLevelRoute: Route = concat(
    validateJson,
    uploadSchema,
    downloadSchema
  )

  private val schemaNotFoundExceptionHandler = ExceptionHandler {
    case _: java.io.IOException =>
      val actionResponse = ActionResponse(ValidateDocumentAction, "", ErrorStatus, Some(SchemaNotFoundMessage))
      val response = createHttpResponse(actionResponse.toJson.prettyPrint, StatusCodes.NotFound)
      complete(response)
    case _: Exception =>
      complete(HttpResponse(InternalServerError, entity = "Unexpected Error fetching schema"))
  }

  def validateJson: Route = {
    pathPrefix(validatePrefix) {
      (post & entity(as[String])) { jsonString: String =>
        handleExceptions(schemaNotFoundExceptionHandler) {
          path(Segment) { schemaId =>

            JsonUtils.loadJsonNode(jsonString) match {
              case Success(jsonNode) =>
                val validationResponse = validateSuccessPath(schemaId, jsonNode)
                complete(validationResponse)

              case Failure(exception) =>
                val actionResponse =
                  ActionResponse(ValidateDocumentAction, schemaId, ErrorStatus, Some(exception.getMessage))

                val response = createHttpResponse(actionResponse.toJson.prettyPrint, StatusCodes.BadRequest)
                complete(response)
            }
          }
        }
      }
    }
  }

  private def validateSuccessPath(schemaId: String, jsonNode: JsonNode): Future[HttpResponse] = {
    SchemaIO.loadSchema(schemaPath, schemaId).map { schema =>

      val cleanedJsonNode = JsonUtils.stripNullValues(jsonNode)

      ValidationJson(cleanedJsonNode, schema).isValid match {
        case Failure(exception) =>
          val response = ActionResponse(ValidateDocumentAction, schemaId, ErrorStatus, Some(exception.getMessage))
          createHttpResponse(response.toJson.prettyPrint, StatusCodes.OK)
        case Success(_) =>
          val response = ActionResponse(ValidateDocumentAction, schemaId, SuccessStatus)
          createHttpResponse(response.toJson.prettyPrint, StatusCodes.OK)
      }
    }
  }

  def uploadSchema: Route = {
    pathPrefix(schemaPrefix) {
      (post & entity(as[String])) { jsonString: String =>
        handleExceptions(schemaNotFoundExceptionHandler) {
          path(Segment) { schemaId =>

            JsonUtils.parseJsonSchema(jsonString) match {
              case Failure(exception) =>

                val actionResponse =
                  ActionResponse(UploadSchemaAction, schemaId, ErrorStatus, Some(exception.getMessage))

                val response = createHttpResponse(actionResponse.toJson.prettyPrint, StatusCodes.BadRequest)
                complete(response)

              case Success(_: JsonSchema) =>

                val actionResponse =
                  SchemaIO.writeSchema(schemaPath, schemaId, jsonString).map { _ =>

                    val actionResponse = ActionResponse(
                      action = UploadSchemaAction,
                      id = schemaId,
                      status = SuccessStatus,
                    )

                    createHttpResponse(actionResponse.toJson.prettyPrint, StatusCodes.Created)
                  }

                complete(actionResponse)
            }
          }
        }
      }
    }
  }

  def downloadSchema: Route = {
    pathPrefix(schemaPrefix) {
      get {
        handleExceptions(schemaNotFoundExceptionHandler) {
          path(Segment) { schemaId =>

            val schema: Future[JsonNode] = SchemaIO.loadSchema(schemaPath, schemaId)
            val response = schema.map { schemaNode: JsonNode =>
              HttpResponse()
                .withStatus(StatusCodes.Created)
                .withEntity(HttpEntity(ContentTypes.`application/json`, schemaNode.toString))
            }

            complete(response)
          }
        }
      }
    }
  }

  private def createHttpResponse(jsonString: String, statusCode: StatusCode): HttpResponse = {

    val entity = HttpEntity(ContentTypes.`application/json`, jsonString)

    HttpResponse()
      .withStatus(statusCode)
      .withEntity(entity)
  }

}
