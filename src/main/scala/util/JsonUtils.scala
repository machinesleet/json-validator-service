package util

import com.fasterxml.jackson.databind.JsonNode

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

}
