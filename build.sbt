lazy val common = Seq(
  organization := "com.gregghz",
  version := "0.0.1-SNAPSHOT",
  scalacOptions ++= Seq("-deprecation", "-Xlint", "-feature", "-Xfatal-warnings")
)

lazy val plugin = project.in(file("./plugin")).settings(common).settings(
  sbtPlugin := true,
  name := "sbt-android-room",
  scalaVersion := "2.10.6",
  addSbtPlugin("org.scala-android" % "sbt-android" % "1.7.7")
)

lazy val library = project.in(file("./library")).settings(common).settings(
  name := "android-room",
  scalaVersion := "2.11.8",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions += "-language:experimental.macros",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    "com.chuusai" %% "shapeless" % "2.3.2" % Test,
    "org.specs2" %% "specs2-core" % "3.8.7" % Test,
    "org.specs2" %% "specs2-mock" % "3.8.7" % Test,
    "android.arch.persistence.room" % "runtime" % "1.0.0-alpha1" % Test
  )
)
