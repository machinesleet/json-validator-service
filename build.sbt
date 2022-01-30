

lazy val root = project
  .in(file("."))
  .settings(
    name := "json-validator-service",

    version := "0.1",

    scalaVersion := "2.13.8",

    libraryDependencies ++= Seq(
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      "org.scalactic" %% "scalactic" % "3.2.10",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
    )
  )