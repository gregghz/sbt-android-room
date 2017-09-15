# sbt-android-room

[![Build Status](https://travis-ci.org/lucidsoftware/sbt-android-room.svg)](https://travis-ci.org/lucidsoftware/sbt-android-room)

This is _work in progress_. Key functionality is likely still missing. Although using it with the [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) seems to work well. The [Lucidchart app](https://play.google.com/store/apps/details?id=com.lucidchart.android.chart) is using it successfully in production.

In your project:

    # project/plugins.sbt
    addSbtPlugin("com.lucidsoftware" % "sbt-android-room" % "0.0.8")

    # build.sbt
    libraryDependencies += "com.lucidsoftware" %% "android-room" % "0.0.8"
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    enablePlugins(AndroidApp, RoomPlugin)

The [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) explains usage. There are a few caveats yo ushould be aware of:

## @Entity()

 You must use the `@RoomEntity` annotation provided by `android-room` instead of `@Entity` (or you will have to create your own setters):

    @RoomEntity()
    case class Thing(@PrimaryKey id: Int, name: String)

## @Query()

You should use the `@RoomQuery` annotation provided by `android-room` instead of `@Query`. It works the same as `@Query`

If you opt to go ahead and use `@Query` directly, when you reference interpolated parameters, instead of referencing them by name, you have to reference them by `arg0`, `arg1`, etc.

    @Dao
    abstract class ThingDao {

      @RoomQuery("SELECT * FROM Thing WHERE id = :id AND name = :name")
      def find(id: Int, name: String)

      @Query("SELECT * FROM Thing WHERE id = :arg0 AND name = :arg1")
      def find(id: Int, name: String)

    }
