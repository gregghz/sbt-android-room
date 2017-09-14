inScope(Global)(Seq(
  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
  developers ++= List(
    Developer("gregg@lucidchart.com", "Gregg Hernandez", "", url("https://github.com/gregghz"))
  ),
  homepage := Some(url("https://github.com/lucidsoftware/sbt-android-room")),
  licenses += "Apache License 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  organization := "com.lucidchart",
  PgpKeys.pgpPassphrase := Some(Array.emptyCharArray),
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/sbt-android-room"), "scm:git:git@github.com:lucidsoftware/sbt-android-room.git")),
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

lazy val common = Seq(
  organization := "com.lucidchart",
  scalacOptions ++= Seq("-deprecation", "-Xlint", "-feature", "-Xfatal-warnings")
)

lazy val plugin = project.in(file("./plugin")).settings(common).settings(
  sbtPlugin := true,
  name := "sbt-android-room",
  scalaVersion := "2.10.6",
  addSbtPlugin("org.scala-android" % "sbt-android" % "1.7.10")
)

lazy val library = project.in(file("./library")).settings(common).settings(
  name := "android-room",
  scalaVersion := "2.11.11",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions += "-language:experimental.macros",
  resolvers += "Google Maven" at "https://maven.google.com",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    "com.chuusai" %% "shapeless" % "2.3.2" % Test,
    "org.specs2" %% "specs2-core" % "3.8.7" % Test,
    "org.specs2" %% "specs2-mock" % "3.8.7" % Test,
    "android.arch.persistence.room" % "runtime" % "1.0.0-alpha9-1" % Test
  )
)
