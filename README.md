# sbt-android-room

[![Build Status](https://travis-ci.org/lucidsoftware/sbt-android-room.svg)](https://travis-ci.org/lucidsoftware/sbt-android-room)

This is _work in progress_. It is not currently production ready so I haven't published an artifact publically. You will need to publish locally to try it out. Key functionality is still missing. Although using it with the [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) seems to work well.

In your project:

    # project/plugins.sbt
    addSbtPlugin("com.lucidsoftware" % "sbt-android-room" % "0.0.4")

    # build.sbt
    libraryDependencies += "com.lucidsoftware" %% "android-room" % "0.0.4"
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    enablePlugins(AndroidApp, RoomPlugin)

The [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) explains usage. There are a few caveats yo ushould be aware of:

## @Entity()

 You must use the `@RoomEntity` annotation provided by `android-room` instead of `@Entity` (or you will have to create your own setters):

    @RoomEntity()
    case class Thing(@PrimaryKey id: Int, name: String)

Case classes annotated with `@RoomEntity` aren't quite normal case classes yet. The following lists enumerate which methods are missing compared to a normal case class.

Instance methods:

- [x] parameterized constructor
- [x] copy
- [x] productPrefix
- [x] productArity
- [x] productElement
- [x] productIterator
- [x] canEqual
- [x] hashCode
- [ ] toString
- [x] equals

Companion methods:

- [ ] toString
- [x] apply
- [ ] unapply

## @Query()

In your DAOs, when you reference interpolated parameters, instead of referencing them by name, you have to reference them by `arg0`, `arg1`, etc.

    @Dao
    abstract class ThingDao {

      @Query("SELECT * FROM Thing WHERE id = :arg0 AND name = :arg1")
      def find(id: Int, name: String)

    }
