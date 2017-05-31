package com.gregghz.room

import org.objectweb.asm._
import sbt._
import scala.collection.mutable
import scala.language.postfixOps

object BaseClassFinder {

  def findExtends(classDir: File, baseClass: String): Seq[String] = {
    val classBuffer = mutable.ListBuffer.empty[String]

    val cv = new ClassVisitor(Opcodes.ASM4) {
      override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]): Unit = {
        if (superName.replace("/", ".") == baseClass) {
          classBuffer += name.replace("/", ".")
        }
      }
    }

    (classDir ** "*.class" get).foreach { entry =>
      Using.fileInputStream(entry) { in =>
        try {
          val r = new ClassReader(in)
          r.accept(cv, 0)
        }
      }
    }

    classBuffer.toList.distinct
  }

}
