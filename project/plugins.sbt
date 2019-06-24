logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.32")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.22")