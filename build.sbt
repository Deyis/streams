name := "bonify_test"

version := "1.0"
 
lazy val `bonify_test` = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      ws,
      guice,
//    Swagger  
      "io.swagger" %% "swagger-play2" % "1.7.1",
      "org.webjars" % "swagger-ui" % "3.17.6",
      "org.scommons.service" %% "scommons-service-play" % "0.1.0-SNAPSHOT",
//    ScalaTest  
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0" % "test",
//    DB related dependencies  
      "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.0.2",
      "com.typesafe.play" %% "play-slick" % "4.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc4",
      "com.typesafe.slick" %% "slick" % "3.3.0" ,
      "com.github.tminglei" %% "slick-pg" % "0.16.3",
      "org.postgresql" % "postgresql" % "42.2.5")
  )
  .enablePlugins(PlayScala, JavaAppPackaging, DockerComposePlugin)
  .disablePlugins(PlayLayoutPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
      
scalaVersion := "2.12.2"

testTagsToExecute := "DockerComposeTestTag"
testExecutionExtraConfigTask := Map("filesrunner.verbose" -> s"true")

fork := true
fork in run := true

dockerImageCreationTask := (publishLocal in Docker).value
dockerBaseImage := "openjdk:9-slim"
dockerExposedPorts := Seq(9000)

mainClass in Compile := Some("play.core.server.ProdServerStart")
