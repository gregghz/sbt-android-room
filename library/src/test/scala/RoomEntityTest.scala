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
      val thing = new Thing()

      thing.setId(11)
      thing.id mustEqual 11

      thing.setName("hello")
      thing.name mustEqual "hello"
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
  }

}
