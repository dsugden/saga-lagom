organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `saga-lagom` = (project in file("."))
  .aggregate(`saga-service-api`, `saga-service-impl`,`ex-one-service-api`,`ex-one-service-impl`)

lazy val `saga-service-api` = (project in file("saga-service-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `saga-service-impl` = (project in file("saga-service-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomLogback,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`saga-service-api`)


lazy val `ex-one-service-api` = (project in file("ex-one-service-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      lagomScaladslApi
    )
  )

lazy val `ex-one-service-impl` = (project in file("ex-one-service-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      lagomLogback,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`ex-one-service-api`, `saga-service-api`)

