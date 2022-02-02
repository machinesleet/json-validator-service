import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor

object JsonValidatorWebService {

  def main(args: Array[String]) : Unit = {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContextExecutor: ExecutionContextExecutor = system.dispatcher

    val routes = new Routes()

    val port = 8080
    val host = "localhost"

    Http()
      .newServerAt(host, port)
      .bind(routes.topLevelRoute)
      .map(_ => println(s"server running at $host:$port"))
  }
}
