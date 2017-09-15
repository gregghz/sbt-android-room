package com.lucidchart.room.query

import scala.annotation.{compileTimeOnly, implicitNotFound, StaticAnnotation}
import scala.reflect.macros.blackbox.Context

@compileTimeOnly("enable macro paradise to expand macro annotations")
class RoomQuery(query: String) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro RoomQueryImpl.annotation

}

class RoomQueryImpl(val c: Context) {

  import c.universe._

  def annotation(annottees: c.Expr[Any]*): Tree = {
    val inputs = annottees.map(_.tree).toList

    val query = extractQuery(c.prefix.tree)

    inputs match {
      case target @ q"$mods def $tname[..$tparams](..$params): $tpt = $expr" :: Nil =>
        val namesToMangledName: Map[String, String] = params.zipWithIndex.map { case (ValDef(_, name, _, _), i) =>
          name.toString -> s"arg$i"
        }(collection.breakOut)

        val fixedQuery = namesToMangledName.foldLeft(query) { case (sofar, (name, mangledName)) =>
          sofar.replaceAll(s":$name", s":$mangledName")
        }

        val newMods = addQueryAnnotation(mods, fixedQuery)

        q"$newMods def $tname[..$tparams](..$params): $tpt = $expr"
      case _ =>
        c.abort(
          c.enclosingPosition,
          "@RoomQuery can only be used on abstract methods"
        )
    }
  }

  private def extractQuery(paramTree: Tree): String = {
    paramTree match {
      case q"new RoomQuery($query)" => query match { case Literal(Constant(value: String)) => value }
      case q"new com.lucidchart.room.query.RoomQuery($query)" => query match { case Literal(Constant(value: String)) => value }
      case _ => c.abort(c.enclosingPosition, "You must provide a query")
    }
  }

  private def addQueryAnnotation(mods: Modifiers, query: String): Modifiers = {
    val Modifiers(flags, privateWithin, annotations) = mods
    val filteredAnnots = annotations.filter {
      case q"new Query(..$p)" => false
      case q"new android.arch.persistence.room.Query(..$p)" => false
      case _ => true
    }

    Modifiers(flags, privateWithin, q"new android.arch.persistence.room.Query($query)" :: filteredAnnots)
  }

}