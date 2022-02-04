import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import model.ActionResponse
import ActionResponse.{ErrorStatus, SchemaNotFoundMessage, SuccessStatus, UploadSchemaAction, ValidateDocumentAction}
import model.ActionResponseProtocol._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import util.SchemaIO

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class JsonValidatorRouteSpec extends AnyFlatSpec
  with Matchers with ScalatestRouteTest with SprayJsonSupport with BeforeAndAfterAll {

  val timeout = 1
  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(new DurationInt(timeout).seconds)

  val schemaPath = "src/test/resources/schemas"
  val routes = new Routes(schemaPath)

  private val validSchemaString =
    """{
      |  "$schema" : "http://json-schema.org/draft-04/schema#",
      |  "type" : "object",
      |  "properties" : {
      |    "source" : {
      |      "type" : "string"
      |    },
      |    "destination" : {
      |      "type" : "string"
      |    }
      |  },
      |  "required" : [ "source", "destination" ]
      |}""".stripMargin


  private val SchemaToWrite = "100"

  it should "load the expected schema from the resources directory given a schema id for a schema that exists" in {

    val expectedSchema: JsonNode = JsonLoader.fromString(validSchemaString)

    val schemaIdThatExists = 42

    Get(s"/schema/$schemaIdThatExists") ~> routes.topLevelRoute ~> check {
      status shouldEqual StatusCodes.Created
      val stringResponse = responseAs[String]
      val jsonNodeFromResponse = JsonLoader.fromString(stringResponse)

      jsonNodeFromResponse shouldEqual expectedSchema
    }
  }

  it should "return a 404 Not Found status code with the expected response string given a schema that does not exist" in {

    val schemaIdThatDoesNotExist = 1234

    Get(s"/schema/$schemaIdThatDoesNotExist") ~> routes.topLevelRoute ~> check {
      status shouldEqual StatusCodes.NotFound

      val actionResponse = responseAs[ActionResponse]

      val expected = ActionResponse(
        action = ValidateDocumentAction,
        id = "",
        status = ErrorStatus,
        message = Some(SchemaNotFoundMessage),
      )

      assert(actionResponse == expected)
    }
  }

  it should "upload a valid JSON schema file at the given id, return the expected response," +
    " and write a file to disk with the expected contents" in {

    val schemaId = SchemaToWrite

    Post(s"/schema/$schemaId", validSchemaString) ~> routes.topLevelRoute ~> check {
      status shouldEqual StatusCodes.Created
      val response = responseAs[ActionResponse]

      val expectedResponse = ActionResponse(
        action = UploadSchemaAction,
        id = schemaId,
        status = SuccessStatus
      )

      assert(response == expectedResponse)

      val schemaFromDisk = Await.result(SchemaIO.loadSchema(schemaPath, schemaId), 2.seconds)

      assert(schemaFromDisk.toPrettyString == validSchemaString)
    }
  }

  it should "not write a file to disk, and return an error response with a Bad Request status code," +
    " when uploading an invalid JSON schema" in {

    val schemaId = "26"

    val invalidSchemaString =
      """{
        |  "type" : "object",
        |  "properties" :
        |    "source" : {
        |      "type" : "string"
        |    },
        |    "destination" : {
        |      "type" : "string"
        |    }
        |  },
        |  "required" : [ "source", "destination" ]
        |}""".stripMargin


    Post(s"/schema/$schemaId", invalidSchemaString) ~> routes.topLevelRoute ~> check {
      status shouldEqual StatusCodes.BadRequest
      val response = responseAs[ActionResponse]

      val expectedErrorMessage =
        "Unexpected character (':' (code 58)): was expecting comma to separate Object entries\n " +
          "at [Source: (StringReader); line: 4, column: 15]"

      val expectedResponse = ActionResponse(
        action = UploadSchemaAction,
        id = schemaId,
        status = ErrorStatus,
        message = Some(expectedErrorMessage)
      )

      assert(response == expectedResponse)

      val filePath = s"$schemaPath/$schemaId.json"
      val file = new File(filePath)

      assert(!file.exists())
    }
  }

  // remove the file we will write to disk a test if it's left over from a previous test run
  override def beforeAll(): Unit = {
    val fileToWrite = new File(s"$schemaPath/$SchemaToWrite.json")

    if (fileToWrite.exists()) fileToWrite.delete()
  }


}
