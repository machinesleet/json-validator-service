import com.github.fge.jackson.JsonLoader
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers
import util.JsonUtils

class JsonUtilsSpec  extends  AnyFlatSpec  with Matchers {

  it should "remove null values from valid json given a file with null values" in {
    val jsonNodeWithNullValues = JsonLoader.fromPath("src/test/resources/valid-config-with-nulls.json")

    val result = JsonUtils.stripNullValues(jsonNodeWithNullValues).toString
    val expected = "{\"source\":\"/home/alice/image.iso\",\"destination\":\"/mnt/storage\",\"chunks\":{\"size\":1024}}"

    assert(result == expected)
  }

  it should "not alter a valid json file without null values" in {
    val jsonNodeWithoutNullValues = JsonLoader.fromPath("src/test/resources/valid-config-without-nulls.json")
    val result = JsonUtils.stripNullValues(jsonNodeWithoutNullValues).toString

    val expected =
      "{\"source\":\"/home/alice/image.iso\",\"destination\":\"/mnt/storage\",\"timeout\":32767,\"chunks\":{\"size\":1024,\"number\":1}}"

    assert(result == expected)
  }



}