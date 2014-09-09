name := "common-messaging"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  Seq(
    "com.typesafe.akka"           %% "akka-actor"           % akkaV,
    "com.typesafe.scala-logging"  %% "scala-logging-slf4j"  % "2.1.2",
    "com.blinkbox.books"          %% "common-json"          % "0.2.1",
    "com.blinkbox.books"          %% "common-scala-test"    % "0.3.0"  % "test",
    "com.typesafe.akka"           %% "akka-testkit"         % akkaV    % "test"
  )
}
