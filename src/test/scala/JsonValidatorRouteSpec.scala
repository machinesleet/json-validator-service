import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

class JsonValidatorRouteSpec extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  val timeout = 1
  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(new DurationInt(timeout).seconds)

  val routes = new Routes()

  it should "load the expected schema from the resources directory given a schema id for a schema that exists" in {

    val expectedSchemaString =
      """
        |{
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
        |}
        """.stripMargin

    val expectedSchema: JsonNode = JsonLoader.fromString(expectedSchemaString)

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
      val stringResponse = responseAs[String]
      assert(stringResponse == "Schema Not Found")
    }
  }

}
