package se.erebusit.dyndb.testkit

import Key.{AttributeTypes, KeyTypes}
import software.amazon.awssdk.services.dynamodb.model._

case class Key(attributeName: String, attributeType: String, keyType: String) {
  require(
    AttributeTypes.contains(attributeType),
    s"attributeType must be one of ${AttributeTypes.mkString(",")}"
  )
  require(
    KeyTypes.contains(keyType),
    s"keyType must be one of ${KeyTypes.mkString(",")}"
  )

  def asAttributeDefinition: AttributeDefinition = AttributeDefinition
    .builder()
    .attributeType(ScalarAttributeType.fromValue(attributeType))
    .attributeName(attributeName)
    .build()

  def asKeySchemaElement: KeySchemaElement = KeySchemaElement
    .builder()
    .keyType(KeyType.fromValue(keyType))
    .attributeName(attributeName)
    .build()
}

object Key {
  val AttributeTypes = Seq("S", "N", "B")
  val KeyTypes = Seq("HASH", "RANGE")
}
