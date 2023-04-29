name := "dynamodblocal-testkit"

version := "0.1.0"
versionScheme := Some("semver-spec")

scalaVersion := "2.13.10"

Global / resolvers += "DynamoDB Local Release Repository" at "https://s3.eu-central-1.amazonaws.com/dynamodb-local-frankfurt/release"

organization := "se.erebusit"

Global / onChangedBuildSource := ReloadOnSourceChanges
publishMavenStyle := true
libraryDependencies ++= Seq(
  "com.amazonaws"          % "DynamoDBLocal" % "1.21.1",
  "software.amazon.awssdk" % "dynamodb"      % "2.20.26" % Provided,
  "org.scalatest"         %% "scalatest"     % "3.2.15"  % Provided
)
