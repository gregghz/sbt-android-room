This is _work in progress_. It is not currently production ready so I haven't published an artifact publically. You will need to publish locally to try it out. Key functionality is still missing. Although using it with the [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) seems to work well.

    git clone git@github.com:gregghz/sbt-android-room.git
    cd sbt-android-room
    sbt plugin/publishLocal library/publishLocal

Then in your own project:

    echo 'addSbtPlugin("com.gregghz" % "sbt-android-room" % "0.0.1-SNAPSHOT")' >> project/plugins.sbt
    echo 'libraryDependencies += "com.gregghz" %% "android-room" % "0.0.1-SNAPSHOT"' >> build.sbt
    echo 'addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)' >> build.sbt

The [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) explains usage. You must use the `@RoomEntity` annotation provided by `android-room` instead of `@Entity` (or you will have to create your own setters):

    @RoomEntity()
    case class Thing(@PrimaryKey id: Int, name: String)

Case classes annotated with `@RoomEntity` aren't quite normal case classes (yet). They are currently missing the ability to pattern match, rely entirely on reference equality, and lack a `copy` method among other issues.
