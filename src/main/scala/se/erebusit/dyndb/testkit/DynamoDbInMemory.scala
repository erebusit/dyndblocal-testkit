package se.erebusit.dyndb.testkit

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import org.scalatest.{BeforeAndAfterAll, Suite}
import DynamoDbInMemory._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{CreateTableRequest, DeleteTableRequest, ProvisionedThroughput}

import java.net.URI
import scala.jdk.CollectionConverters.SeqHasAsJava

object DynamoDbInMemory {
  val SqlLite4JavaProperty = "sqlite4java.library.path"
  val DynDbInMemoryPortProperty = "dyndb.server.port"
  val DefaultPort: Int = Option(
    System.getProperty(DynDbInMemoryPortProperty)
  ) match {
    case Some(v) if v.nonEmpty => v.toInt
    case _                     => sys.env.getOrElse("DYN_DB_PORT", "8000").toInt
  }
  val AwsRegionEnvName = "AWS_REGION"
  val DefaultAwsRegionValue = "eu-west-1"
  val DefaultCredentialsValue = "placeholder"
}

trait DynamoDbInMemory extends BeforeAndAfterAll {
  this: Suite =>

  Option(System.getProperty(SqlLite4JavaProperty)) match {
    case Some(v) if v.nonEmpty => ()
    case _ =>
      System.setProperty(
        SqlLite4JavaProperty,
        sys.env.getOrElse("DYN_DB_NATIVELIBS", "native-libs")
      )
  }

  if (System.getProperty(SqlLite4JavaProperty).isEmpty)
    System.setProperty(SqlLite4JavaProperty, "native-libs")

  private val awsRegion =
    sys.env.getOrElse(AwsRegionEnvName, DefaultAwsRegionValue)

  private val port = DefaultPort
  private val server = ServerRunner.createServerFromCommandLineArgs(
    Array("-inMemory", "-port", port.toString)
  )

  private val client = DynamoDbClient
    .builder()
    .endpointOverride(URI.create(s"http://localhost:$port"))
    .region(Region.of(awsRegion))
    .credentialsProvider(
      StaticCredentialsProvider.create(
        AwsBasicCredentials
          .create(DefaultCredentialsValue, DefaultCredentialsValue)
      )
    )
    .build()

  /** Returns a DynamoDbClient implementation to use against the in memory database. Only compatible with AWS SDK v2 at the moment.
    * @return DynamoDbClient
    */
  def dbClient: DynamoDbClient = client

  /** Creates a new table with the provided attribute definitions and key schema. By default, it will create a table
    * without any global indexes or local secondary indexes. It will also create the table with a Provisioned throughput
    * set to 10 reads and 10 writes.
    *
    * @param tableName String representation of the table name, which must be provided
    * @param primaryKey Primary key for the table, required
    * @param rangeKey Range key for the table, optional
    */
  def createTable(
    tableName: String,
    primaryKey: Key,
    rangeKey: Option[Key]
  ): Unit = {
    val keys = Seq(primaryKey) ++ {
      rangeKey match {
        case Some(rKey) => Seq(rKey)
        case None       => Seq()
      }
    }

    client.createTable(
      CreateTableRequest
        .builder()
        .tableName(tableName)
        .attributeDefinitions(
          keys.map(f => f.asAttributeDefinition).toList.asJava
        )
        .keySchema(keys.map(k => k.asKeySchemaElement).toList.asJava)
        .provisionedThroughput(
          ProvisionedThroughput
            .builder()
            .readCapacityUnits(10)
            .writeCapacityUnits(10)
            .build()
        )
        .build()
    )
  }

  /** Removes the table. Call this after each test if you wish to clear your table between tests.
    * @param tableName Tablename to delete
    */
  def deleteTable(tableName: String): Unit = client.deleteTable(
    DeleteTableRequest.builder().tableName(tableName).build()
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }
}
