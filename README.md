## Installation

```sbt
libraryDependencies += "io.quickci.dyndb" %% "dynamodblocal-testkit" % "0.2.1" % Test

```

This will include the DynamoDB local dependency. Other dependencies that are required but not included
are `org.scalatest:scalatest` and `software.amazon.awssdk:dynamodb`.

## Usage

The basic idea is to extend the `DynamoDbInMemory` trait and then call `dbClient` to get the `DynamoDbClient`
implementation, which you then can inject into your tested class.

If you want to clear the table between each test, extend `BeforeAndAfterEach` and call `createTable/deleteTable` in
their respective methods. Since the `dbClient` method returns a `DynamoDbClient` implementation, you can provision your
tables and do everything as if you were calling the real service.

```scala
import io.quickci.dyndb.testkit.DynamoDbInMemory
import org.scalatest.BeforeAndAfterEach

class MyDbTest
  extends AnyFlatSpec
    with DynamoDbInMemory
    with BeforeAndAfterEach {
  override def beforeEach(): Unit = {
    super.beforeEach()
    createTable(
      TableName,
      Key("PK", "S", "HASH"),
      Some(Key("SK", "S", "RANGE"))
    )
  }

  override def afterEach(): Unit = {
    super.afterEach()
    deleteTable(TableName)
  }

  it should "do db testing" in {
    val inMemoryClient = dbClient
    val myDb = new MyDbClass(inMemoryClient)
    // Test code //
  }
}

```

### Native libs

This library requires native libs for SqlLite to be able to function. These are expected to be found in
the `native-libs` folder in your module directory. To create these files, add this to your module `build.sbt`:

```sbt
val sqlite4java = "1.0.392"
libraryDependencies ++= Seq(
  "com.almworks.sqlite4java" % "sqlite4java" % sqlite4java % "test",
  "com.almworks.sqlite4java" % "sqlite4java-win32-x86" % sqlite4java % "test",
  "com.almworks.sqlite4java" % "sqlite4java-win32-x64" % sqlite4java % "test",
  "com.almworks.sqlite4java" % "libsqlite4java-osx" % sqlite4java % "test",
  "com.almworks.sqlite4java" % "libsqlite4java-linux-i386" % sqlite4java % "test",
  "com.almworks.sqlite4java" % "libsqlite4java-linux-amd64" % sqlite4java % "test"
) // Remove the dependencies for the architectures and OS:es you don't need

lazy val copyJars = taskKey[Unit]("copyJars")
copyJars := {
  import java.io.File
  import java.nio.file.Files
  val artifactTypes = Set("dylib", "so", "dll")
  val files = Classpaths.managedJars(Test, artifactTypes, update.value).files
  val nativelibsDir = s"${baseDirectory.value}${File.separator}native-libs"
  Files.createDirectories(new File(nativelibsDir).toPath)
  files.foreach { f =>
    val fileToCopy = new File(nativelibsDir, f.name)
    if (!fileToCopy.exists()) {
      Files.copy(f.toPath, fileToCopy.toPath)
    }
  }
}
(Compile / compile) := (Compile / compile).dependsOn(copyJars).value

```

If you wish to store these in another location than `native-libs`, modify the path in the text above and tell the
testkit to look for them in your directory by using the configuration keys documented further down.

## Configurable properties

Configuration values will be determined in the following priority:

1. System properties
1. Environment variables
1. Default values

| System property | Environment variable | Type | Default | Description  |
| --- | --- | --- | --- | --- |
| dyndb.server.port | DYN_DB_PORT | Int | `8000` | Sets the port on which the server will bind |
| sqlite4java.library.path | DYN_DB_NATIVELIBS | String | `native-libs` | Location of the native library files for sqlite |

## Roadmap

* Support for global indexes
* Support for secondary local indexes
* Better  