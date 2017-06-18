package com.lucidchart.room

import java.nio.file.Files
import sbinary.DefaultProtocol.StringFormat
import sbt._
import sbt.Cache.seqFormat
import sbt.Keys._
import scala.collection.JavaConverters._
import xsbt.api._

object RoomProcessing {

  lazy val processRoomAnnotations: TaskKey[Unit] =
    TaskKey[Unit]("processRoomAnnotations", "Room annotation processor")

  lazy val tasks = Seq(
    processRoomAnnotations := {
      val log = streams.value.log

      log.info("Processing room annotations ...")

      val classpath =
        ((products in Compile).value ++ ((dependencyClasspath in Compile).value.files)).mkString(":")

      implicit val outputConverter = android.Keys.outputLayout.value
      val destinationDirectory = android.Keys.projectLayout.value.gen
      val classDestDirectory = (classDirectory in Compile).value
      val classesToProcess = BaseClassFinder.findExtends(classDestDirectory, "android.arch.persistence.room.RoomDatabase").mkString(" ")
      val bootcp = android.Keys.bootClasspath.value.map(_.data.absolutePath).mkString(java.io.File.pathSeparator)
      val target = "-source 1.7 -target 1.7"

      log.info(s"Processing classes: $classesToProcess")

      val annotationProcessorCommand =
        s"javac $target -cp $classpath -bootclasspath $bootcp -proc:only -XprintRounds -d $destinationDirectory $classesToProcess"
      failIfNonZeroExitStatus(annotationProcessorCommand, "Failed to process room annotations.", log)

      val impls = Files.walk(destinationDirectory.toPath).iterator().asScala.filter(_.toString.endsWith("_Impl.java")).mkString(" ")

      val annotationCompilerCommand = s"javac $target -cp $classpath -bootclasspath $bootcp -d $classDestDirectory $impls"
      failIfNonZeroExitStatus(annotationCompilerCommand, "Failed to process annotations.", log)
    }
  )

  private def failIfNonZeroExitStatus(command: String, message: => String, log: Logger): Unit = {
    val result = command.!

    if (result != 0) {
      log.error(message)
      sys.error(s"Failed running command: $command")
    }
  }

}
