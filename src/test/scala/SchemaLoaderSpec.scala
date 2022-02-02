import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers
import util.SchemaLoader

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SchemaLoaderSpec extends AnyFlatSpec with Matchers with ScalaFutures {

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

    val schemaLoadResult = Await.result(SchemaLoader.loadSchema(schemaId = 42), 5.seconds)

    assert(schemaLoadResult == expectedSchema)
  }

  it should "fail to load a schema that does not exist" in {
    assertThrows[java.io.IOException] {
      Await.result(SchemaLoader.loadSchema(schemaId = 53), 5.seconds)
    }
  }


}