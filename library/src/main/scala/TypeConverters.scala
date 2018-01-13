package com.lucidchart.room.types

import scala.annotation.{compileTimeOnly, implicitNotFound, StaticAnnotation}
import scala.reflect.macros.blackbox.Context
import scala.language.higherKinds

@compileTimeOnly("enable macro paradise to expand macro annotations")
class InferTypeConverters(query: String) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro InferTypeConvertersImpl.annotation

}

class InferTypeConvertersImpl(val c: Context) {

  import c.universe._

  def annotation(annottees: c.Expr[Any]*): Tree = {
    annottees.map(_.tree).toList match {
      case q"$mods class $tpname extends { ..$earlydefns } with StringTypeConvertersFor[$targetType] { $self => ..$stats }" :: Nil =>

        val targets = extractTargetTypes(c.prefix.tree)

        val methods = targets.flatMap { wrappedType =>
          val toName = TermName(s"wrapperOf${wrappedType.toString}ToString")
          val fromName = TermName(s"wrapperOf${wrappedType.toString}FromString")
          val wrapped = TypeName(wrappedType.toString)

          List(
            q"""
              @android.arch.persistence.room.TypeConverter
              def $toName(arg: $targetType[$wrapped]): String = this.to(arg)
            """,
            q"""
              @android.arch.persistence.room.TypeConverter
              def $fromName(str: String): $targetType[$wrapped] = this.from(str)
            """
          )
        }

        q"$mods class $tpname extends { ..$earlydefns } with StringTypeConvertersFor[$targetType] { $self => ..$stats; ..$methods }"

      case _ => c.abort(c.enclosingPosition, "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor")
    }
  }

  private def extractTargetTypes(prefix: Tree): List[Tree] = {
    prefix match {
      case q"new InferTypeConverters(Array(..$targets))" => targets
      case _ => Nil
    }
  }

}

trait StringTypeConvertersFor[A[_]] {
  def to[T](value: A[T]): String
  def from[T](str: String): A[T]
}