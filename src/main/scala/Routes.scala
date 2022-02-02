import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.Materializer
import com.fasterxml.jackson.databind.JsonNode
import util.SchemaLoader

import scala.concurrent.{ExecutionContextExecutor, Future}

class Routes(implicit mat: Materializer, e: ExecutionContextExecutor) {

  private val schemaPrefix = "schema"

  val topLevelRoute: Route = concat(
    downloadSchema
  )

  private val schemaNotFoundExceptionHandler = ExceptionHandler {
    case _: java.io.IOException =>
      complete(HttpResponse(NotFound, entity = "Schema Not Found"))
    case _: Exception =>
      complete(HttpResponse(InternalServerError, entity = "Unexpected Error fetching schema"))
  }

  def downloadSchema: Route = {
    pathPrefix(schemaPrefix) {
      handleExceptions(schemaNotFoundExceptionHandler) {
        path(IntNumber) { schemaId =>
          get {
            val schema: Future[JsonNode] = SchemaLoader.loadSchema(schemaId)
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

}
