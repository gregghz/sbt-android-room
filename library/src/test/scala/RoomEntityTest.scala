package com.lucidchart.room.entity

import android.arch.persistence.room._
import org.specs2.mutable.Specification
import shapeless.test.illTyped

class RoomEntityTest extends Specification {

  @RoomEntity
  case class Thing(
    @PrimaryKey id: Int,
    name: String
  )

  object Thing {
    def f() = "f"
  }

  "RoomEntity" should {
    "create setters" in {
      val thing = new Thing(1, "hi")

      thing.setId(11)
      thing.id mustEqual 11

      thing.setName("hello")
      thing.name mustEqual "hello"
    }

    "have valid constructors" in {
      val thing = new Thing(11, "hello")

      thing.id mustEqual 11
      thing.name mustEqual "hello"

      val x = new Thing()
      x.setId(11)
      x.setName("hello")

      x.id mustEqual 11
      x.name mustEqual "hello"
    }

    "create apply method" in {
      val thing = Thing(10, "hello")

      thing.id mustEqual 10
      thing.name mustEqual "hello"
    }

    "preserve companion object methods" in {
      Thing.f() mustEqual "f"
    }

    "use value equality" in {
      val x = Thing(1, "hi")
      val y = Thing(1, "hi")

      x mustEqual y

      val z = Thing(1, "hello")

      x mustNotEqual z
    }

    "copy" in {
      val x = Thing(1, "hi")

      val y = x.copy(id = 2)

      y mustEqual Thing(2, "hi")

      val z = x.copy(name = "hello")

      z mustEqual Thing(1, "hello")

      val w = x.copy(id = 10, name = "hey")

      w mustEqual Thing(10, "hey")
    }

    "work with more than two parameters" in {
      @RoomEntity case class Three(@PrimaryKey a: Int, b: Int, c: Int)

      val x = Three(1, 2, 3)
      x.a mustEqual 1
      x.b mustEqual 2
      x.c mustEqual 3
    }

    "pass through params without blowing up" in {
      @RoomEntity(primaryKeys = Array("id", "name"))
      case class Other(id: Int, name: String)

      success
    }
  }

}
