lazy val root = (project in file(".")).
  settings(
    name := "common-messaging",
    organization := "com.blinkbox.books",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      Seq(
        "com.typesafe.akka"           %% "akka-actor"           % akkaV,
        "com.typesafe.scala-logging"  %% "scala-logging-slf4j"  % "2.1.2",
        "com.blinkbox.books"          %% "common-json"          % "0.2.4",
        "com.blinkbox.books"          %% "common-scala-test"    % "0.3.0"   %  Test,
        "com.typesafe.akka"           %% "akka-testkit"         % akkaV     %  Test
      )
    }
  )
