//https://github.com/xerial/sbt-sonatype
sonatypeProfileName := "se.erebusit"
publishMavenStyle := true
licenses := Seq(
  "GNU GPLv3" -> url("https://spdx.org/licenses/GPL-3.0-or-later.html")
)

usePgpKeyHex("1969EF406F94F6C8F284078038829F785415D6E6")

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(
  GitHubHosting("erebusit", "dyndblocal-testkit", "opensource@erebusit.se")
)
