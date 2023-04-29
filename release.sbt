//https://github.com/sbt/sbt-release
import ReleaseTransformations._

releaseCrossBuild := false // true if you cross-build the project for multiple Scala versions
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
//   For non cross-build projects, use releaseStepCommand("publishSigned")
  releaseStepCommand("publishSigned"),
//  releaseStepCommandAndRemaining("+publishSigned"),
//  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion
//  pushChanges
)
