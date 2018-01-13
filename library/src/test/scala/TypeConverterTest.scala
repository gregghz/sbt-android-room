package com.lucidchart.room.types

import android.arch.persistence.room._
import org.specs2.mutable.Specification
import shapeless.test.illTyped

case class Test[A](val str: String)

@InferTypeConverters(Array(Int, String, Float))
class TypeConverters extends StringTypeConvertersFor[Test] {
  def to[A](value: Test[A]): String = value.str
  def from[A](str: String): Test[A] = new Test[A](str)
}

class ImplicitResolutionTest extends Specification {

  "InferTypeConverters" should {
    "generate all the methods" in {
      val converters = new TypeConverters

      converters.wrapperOfIntToString(new Test[Int]("hello")) mustEqual "hello"
      converters.wrapperOfIntFromString("hello") mustEqual new Test[Int]("hello")

      converters.wrapperOfStringToString(new Test[String]("hello")) mustEqual "hello"
      converters.wrapperOfStringFromString("hello") mustEqual new Test[String]("hello")

      converters.wrapperOfFloatToString(new Test[Float]("hello")) mustEqual "hello"
      converters.wrapperOfFloatFromString("hello") mustEqual new Test[Float]("hello")
    }

    "should not compile on anything that isn't a class" in {
      illTyped(
        """@InferTypeConverters(Array(Int)) trait Test2""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) case class Test2()""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) abstract class Test2()""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) object Test2""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) val x = 10""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) var x = 10""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      illTyped(
        """@InferTypeConverters(Array(Int)) def x = 10""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      success
    }

    "require the class extends StringTypeConvertersFor" in {
      illTyped(
        """@InferTypeConverters(Array(Int)) class NoParent""",
        "@InferTypeConverters must be used on a plain class that extends StringTypeConvertersFor"
      )

      success
    }
  }

}