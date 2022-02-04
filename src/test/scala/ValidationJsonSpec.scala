import com.github.fge.jackson.JsonLoader
import jsonValidator.model.ValidationJson
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers

class ValidationJsonSpec extends AnyFlatSpec with Matchers {

  private val validSchema = JsonLoader.fromPath("src/test/resources/schemas/valid-config-schema.json")

  it should "return success given a file that matches the schema" in {
    val validFileToTest = JsonLoader.fromPath("src/test/resources/valid-config-with-nulls.json")

    val result = ValidationJson(validFileToTest, validSchema).isValid
    assert(result.isSuccess)
  }

  it should "return failure given a file where a required field defined by the schema is missing (source)" in {
    val invalidFileToTest = JsonLoader.fromPath("src/test/resources/invalid-config.json")

    val result2 = ValidationJson(invalidFileToTest, validSchema).isValid
    assert(result2.isFailure)
  }

}
