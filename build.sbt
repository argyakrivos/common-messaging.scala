name := "common-messaging"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  val akkaV = "2.3.2"
  val json4sV = "3.2.10"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"    % akkaV % "test",
    "com.typesafe"        %%  "scalalogging-slf4j" % "1.1.0",
    "joda-time"           %   "joda-time"       % "2.3",
    "org.joda"            %   "joda-convert"    % "1.6",
    "org.json4s"          %%  "json4s-jackson"  % json4sV,
    "org.json4s"          %%  "json4s-ext"      % json4sV,
    "org.mockito"         %   "mockito-core"    % "1.9.5" % "test",
    "org.scalatest"       %%  "scalatest"       % "2.1.6" % "test",
    "junit"               %   "junit"           % "4.11"  % "test",
    "com.novocode"        %   "junit-interface" % "0.10"  % "test"
  )
}
