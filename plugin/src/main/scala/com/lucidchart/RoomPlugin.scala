package com.lucidchart.room

import sbt._
import sbt.Keys._

object RoomPlugin extends AutoPlugin {

  override def requires = android.AndroidApp

  override def projectSettings: Seq[Setting[_]] = RoomProcessing.tasks ++ settings ++ dependencies

  val settings = Seq(
    packageBin in Compile :=
      (packageBin in Compile).dependsOn(RoomProcessing.processRoomAnnotations in Compile).value
  )

  val dependencies = Seq(
    resolvers += "Google Maven" at "https://maven.google.com",
    libraryDependencies ++= Seq(
      "android.arch.persistence.room" % "compiler" % "1.0.0-alpha9-1" % Provided,
      "android.arch.persistence.room" % "runtime" % "1.0.0-alpha9-1"
    )
  )

}
