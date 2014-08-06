name := "common-messaging"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  val akkaV = "2.3.2"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"          % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"        % akkaV % "test",
    "com.typesafe"        %%  "scalalogging-slf4j"  % "1.1.0",
    "com.blinkbox.books"  %%  "common-json"         % "0.1.1",
    "com.blinkbox.books"  %%  "common-scala-test"   % "0.2.0" % "test"
  )
}
