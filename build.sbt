sbtPlugin := true

organization := "com.gregghz"

name := "sbt-android-room"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.6"

scalacOptions ++= Seq("-deprecation", "-Xlint", "-feature", "-Xfatal-warnings")

addSbtPlugin("org.scala-android" % "sbt-android" % "1.7.7")

libraryDependencies += "org.ow2.asm" % "asm-all" % "5.0.4"