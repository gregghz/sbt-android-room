package com.lucidchart.room.entity

import scala.annotation.{compileTimeOnly, implicitNotFound, StaticAnnotation}
import scala.reflect.macros.blackbox.Context

@compileTimeOnly("enable macro paradise to expand macro annotations")
class RoomEntity(primaryKeys: Array[String]) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro RoomEntityImpl.annotation

}

class RoomEntityImpl(val c: Context) {

  import c.universe._

  def annotation(annottees: c.Expr[Any]*): Tree = {

    val inputs = annottees.map(_.tree).toList

    val parameters = extractParams(c.prefix.tree)

    inputs match {
      case target @ q"$mods class $tpname[..$tparams] $ctorMods(..$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        if (mods.hasFlag(Flag.CASE)) {
          processCaseClass(target.head, tail.headOption, parameters)
        } else {
          processClass(target.head, tail.headOption, q"", parameters)
        }
      case _ =>
        c.abort(
          c.enclosingPosition,
          "@RoomEntity must be used on a case class with a single parameter list"
        )
    }

  }

  private case class Params(foreignKeys: List[Tree]) {
    def tree: Tree = q"..$foreignKeys"
  }

  private def extractParams(paramTree: Tree): Tree = {
    paramTree match {
      case q"new RoomEntity(..$params)" => q"..$params"
      case q"new com.lucidchart.room.entity.RoomEntity(..$params)" => q"..$params"
      case _ => q""
    }
  }

  private def processCaseClass(caseClass: Tree, companion: Option[Tree], parameters: Tree): Tree = {
    val q"""$mods class $tpname[..$tparams] $ctorMods(..$params)
              extends { ..$earlydefns } with ..$parents { $self => ..$stats }""" = caseClass

    val newMods = removeCaseMod(mods)

    val paramNames = params.map { case q"..$annots val $name: $tpe = $rhs" =>
      name.toString
    }.toSet

    val vars = valsToVars(params)

    val caseClassImpl = generateCaseClassImplementation(params, tpname)
    val newCtor = constructorImplementation(params, tpname)

    val rawClass = q"""$newMods class $tpname[..$tparams] $ctorMods() extends { ..$earlydefns } with ..$parents { $self => ..$stats; ..$vars; ..$caseClassImpl}"""

    processClass(rawClass, Some(inflateCaseClassCompanion(tpname.toTermName, params, companion)), newCtor, parameters)
  }

  private def processClass(caseClass: Tree, companion: Option[Tree], newCtor: Tree, parameters: Tree): Tree = {
    val q"""$mods class $tpname[..$tparams] $ctorMods(..$params)
              extends { ..$earlydefns } with ..$parents { $self => ..$stats }""" = caseClass

    val init: List[Tree] = Nil
    val setters = stats.foldLeft(init) {
      case (methods, q"..$annots var $name: $tpe = $rhs") =>
        val paramName = TermName(c.freshName)
        val methodName = TermName(s"set${name.toString.capitalize}")

        methods :+ q"""def $methodName($paramName: $tpe): Unit = $name = $paramName"""
      case (methods, _) => methods
    }

    val newMods = addEntityAnnotation(mods, parameters)
    val comp = companion.getOrElse(q"object ${tpname.toTermName}")

    // https://stackoverflow.com/questions/22756542/how-do-i-add-a-no-arg-constructor-to-a-scala-case-class-with-a-macro-annotation
    val body = stats ++ setters
    val defaultCtorPos = c.enclosingPosition
    val newCtorPos = defaultCtorPos
      .withEnd(defaultCtorPos.end + 1)
      .withStart(defaultCtorPos.start + 1)
      .withPoint(defaultCtorPos.point + 1)
    val newBody = body :+ atPos(newCtorPos)(newCtor)

    val result = q"""$newMods class $tpname[..$tparams] $ctorMods(..$params)
        extends { ..$earlydefns } with ..$parents { $self => ..$newBody }"""

    q"$result; $comp"
  }

  private def addEntityAnnotation(mods: Modifiers, params: Tree): Modifiers = {
    val Modifiers(flags, privateWithin, annotations) = mods
    val filteredAnnots = annotations.filter {
      case q"new Entity(..$p)" => false
      case q"new android.arch.persistence.room.Entity(..$p)" => false
      case _ => true
    }

    Modifiers(flags, privateWithin, q"new android.arch.persistence.room.Entity(..$params)" :: filteredAnnots)
  }

  private def removeCaseMod(mods: Modifiers): Modifiers = {
    val Modifiers(flags, privateWithin, annotations) = mods
    val finalFlags = removeFlag(flags, Flag.CASE)
    Modifiers(finalFlags, privateWithin, annotations)
  }

  private def generateCaseClassImplementation(params: List[Tree], tpname: TypeName): List[Tree] = {
    productImplementation(params, tpname) ++
      valueEqualityImplementation(params, tpname) ++
      List(copyImplementation(params, tpname))
  }

  private def constructorImplementation(params: List[Tree], tpname: TypeName): Tree = {
    val names = params.map { case q"..$mods val $name: $tpe = $rhs" =>
      (name, tpe)
    }
    val setterCalls = names.map { case (name, _) =>
      val term = TermName(s"set${name.toString.capitalize}")
      q"$tpname.this.$term($name)"
    }

    val start = q"@android.arch.persistence.room.Ignore() def this() = { this(); ..$setterCalls }"

    names.foldLeft(start) {
      case (q"@android.arch.persistence.room.Ignore() def this(..$constParams) = { this(); ..$body }", (name, tpe)) =>
        if (constParams.isEmpty) {
          q"@android.arch.persistence.room.Ignore() def this($name: $tpe) = { this(); ..$body }"
        } else {
          q"@android.arch.persistence.room.Ignore() def this(..$constParams, $name: $tpe) = { this(); ..$body }"
        }
    }
  }

  private def productImplementation(params: List[Tree], tpname: TypeName): List[Tree] = {
    val productArity = q"def productArity: Int = ${params.length}"

    val productElement = {
      val cases = params.zipWithIndex.map { case (q"..$mods val $name: $tpe = $rhs", i) =>
        cq"$i => $name: Any"
      }
      q"def productElement(n: Int): Any = n match { case ..$cases; case _ => throw new java.lang.IndexOutOfBoundsException(n.toString) }"
    }

    val canEqual = q"def canEqual(that: Any): Boolean = that.isInstanceOf[$tpname]"

    val iterator = {
      val names = params.map { case q"..$mods val $name: $tpe = $rhs" =>
        q"$tpname.this.$name"
      }
      q"override def productIterator: Iterator[Any] = List[Any](..$names).toIterator"
    }

    val prefix = {
      val strLiteral = q"${tpname.toString}"
      q"override def productPrefix: String = $strLiteral"
    }

    List(productArity, productElement, canEqual, iterator, prefix)
  }

  private def valueEqualityImplementation(params: List[Tree], tpname: TypeName): List[Tree] = {

    def conjunction(comps: List[Tree]): Tree = {
      comps.tail.foldLeft(comps.head) { case (sofar, next) =>
        q"$sofar && $next"
      }
    }

    val hashCode = {
      val baseHash = tpname.hashCode
      val result = TermName(c.freshName)

      val calc = params.map { case q"..$mods val $name: $tpe = $rhs" =>
        q"$result = 37 * $result + $tpname.this.${name}.hashCode"
      }

      q"override def hashCode(): Int = { var $result: Int = $baseHash; ..$calc; $result }"
    }

    val equals = {
      val that = TermName(c.freshName)
      val eq = q"$tpname.this.eq($that.asInstanceOf[Object])"
      val matchType = q"""
        $that match {
          case (_: $tpname) => true
          case _ => false
        }
      """

      lazy val otherName = TermName(c.freshName)

      lazy val paramComps = params.map { case q"..$mods val $name: $tpe = $rhs" =>
        q"$tpname.this.$name == $otherName.$name"
      }

      q"""override def equals($that: Any): Boolean = {
        ($eq || $matchType) && {
            val $otherName: $tpname = $that.asInstanceOf[$tpname]
            ${conjunction(paramComps)}
          }
      }
      """
    }

    List(hashCode, equals)
  }

  private def copyImplementation(params: List[Tree], tpname: TypeName): Tree = {
    val term = tpname.toTermName
    val names = params.map { case q"..$mods val $name: $tpe = $rhs" =>
      name
    }
    val start = q"def copy(): $tpname = $term.apply(..$names)"

    params.foldLeft(start) {
      case (q"def copy(..$params): $tp1 = $tp2(..$n)", q"..$mods val $name: $tpe = $rhs") =>
        if (params.isEmpty) {
          q"def copy($name: $tpe = $tpname.this.$name) = $term.apply(..$names)"
        } else {
          q"def copy(..$params, $name: $tpe = $tpname.this.$name) = $term.apply(..$names)"
        }

    }
  }

  private def inflateCaseClassCompanion(name: TermName, params: List[Tree], companion: Option[Tree]): Tree = {
    val comp = companion.getOrElse(q"object $name")

    val q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" = comp

    val apply = {
      val args = params.map {
        case q"..$mods val $name: $tpe = $rhs" => q"$name: $tpe"
      }

      val tempName = TermName(c.freshName)
      val sets = params.map {
        case q"..$mods val $name: $tpe = $rhs" =>
          val methodName = TermName(s"set${name.toString.capitalize}")
          q"$tempName.$methodName($name)"
      }

      q"def apply(..$args): ${name.toTypeName} = new ${name.toTypeName}(..$args)"
    }

    q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body; $apply }"
  }

  private def valsToVars(params: List[Tree]): List[Tree] = {
    params.map {
      case q"..$mods val $name: $tpe = $rhs" =>
        // we need to remove case class stuff too
        val Modifiers(flags, privateWithin, annotations) = mods
        val cleanFlags = removeFlag(removeFlag(flags, Flag.CASEACCESSOR), Flag.PARAMACCESSOR)
        val cleanMods = Modifiers(cleanFlags, privateWithin, annotations)

        q"$cleanMods var $name: $tpe = $rhs"
      case stat =>
        stat
    }
  }

  private def removeFlag(flags: FlagSet, removing: FlagSet): FlagSet = {
    (flags.asInstanceOf[Long] ^ removing.asInstanceOf[Long]).asInstanceOf[FlagSet]
  }

}
