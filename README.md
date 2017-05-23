    git clone git@github.com:gregghz/sbt-android-room.git
    cd sbt-android-room
    sbt publishLocal

Then in your own project:

    echo 'addSbtPlugin("com.gregghz" % "sbt-android-room" % "0.0.1-SNAPSHOT")' >> project/plugins.sbt

The [Room Persistence Library documentation](https://developer.android.com/topic/libraries/architecture/room.html) explains usage. One caveat for now is that you must provide setters manually and give default values for `@Entity` to work correctly:

    @Entity()
    class Thing {
      @PrimaryKey var id: Int = _
      var name: String = _

      def setId(_id: Int) = id = _id
      def setName(_name: String) = name = _name
    }
