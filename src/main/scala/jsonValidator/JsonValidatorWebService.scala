package jsonValidator

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object JsonValidatorWebService {

  // server example from https://doc.akka.io/docs/akka-http/current/introduction.html
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContextExecutor: ExecutionContextExecutor = system.dispatcher

    val schemaPath = "src/main/resources/schemas"
    val routes = new Routes(schemaPath)

    val port = 8080
    val host = "localhost"

    val binding = Http()
      .newServerAt(host, port)
      .bind(routes.topLevelRoute)
      .map { binding =>
        println(s"server running at $host:$port")
        println("Press RETURN to stop...")
        binding
      }

    StdIn.readLine() // let it run until user presses return

    binding
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
