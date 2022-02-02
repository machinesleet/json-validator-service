

lazy val root = project
  .in(file("."))
  .settings(
    name := "json-validator-service",

    version := "0.1",

    scalaVersion := "2.13.8",

    libraryDependencies ++= {

      val AkkaVersion = "2.6.8"
      val AkkaHttpVersion = "10.2.7"

      Seq(
        "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
        "com.typesafe.akka" %% "akka-http" % "10.2.7",
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
        "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
        "org.scalactic" %% "scalactic" % "3.2.10",
        "org.scalatest" %% "scalatest" % "3.2.10" % "test",
        "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test",
        "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test",
      )
    }
  )