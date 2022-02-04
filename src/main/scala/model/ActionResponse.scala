package model

import spray.json._

case class ActionResponse(action: String,
                          id: String,
                          status: String,
                          message: Option[String] = None)

object ActionResponse {
  val UploadSchemaAction = "uploadSchema"
  val ValidateDocumentAction = "validateDocument"

  val ErrorStatus = "error"
  val SuccessStatus = "success"

  val SchemaNotFoundMessage = "schema not found"
}

object ActionResponseProtocol extends DefaultJsonProtocol {

  implicit object ActionResponseFormat extends RootJsonFormat[ActionResponse] {

    val Action = "action"
    val Id = "id"
    val Status = "status"
    val Message = "message"

    def write(ar: ActionResponse): JsObject = {
      ar.message match {
        case Some(definedMessage) =>
          JsObject(
            Action -> JsString(ar.action),
            Id -> JsString(ar.id),
            Status -> JsString(ar.status),
            Message -> JsString(definedMessage)
          )
        case None =>
          JsObject(
            Action -> JsString(ar.action),
            Id -> JsString(ar.id),
            Status -> JsString(ar.status),
          )
      }
    }

    def read(value: JsValue): ActionResponse = {
      value.asJsObject.getFields(Action, Id, Status, Message) match {
        case Seq(JsString(action), JsString(id), JsString(status), JsString(message)) =>
          new ActionResponse(action, id, status, Some(message))
        case Seq(JsString(action), JsString(id), JsString(status)) =>
          new ActionResponse(action, id, status, None)
        case _ =>
          throw new DeserializationException("Error deserialising Action Response")
      }
    }
  }

}
