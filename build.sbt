name := "Siddhanth Venkateshwaran HW1"
version := "0.1"
scalaVersion := "2.12.10"

// Store dependency group names, artifact names and versions to be added to the project
lazy val cloudsimplus = "org.cloudsimplus" % "cloudsim-plus" % "5.4.3"
lazy val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.3"
lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val slf4j = "org.slf4j" %"slf4j-api" %"1.7.30" % "test"
lazy val typesafe = "com.typesafe" % "config" % "1.4.0"

// Configure this project's settings
lazy val thisProject = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(cloudsimplus, logbackCore, logbackClassic, slf4j, typesafe)
  )