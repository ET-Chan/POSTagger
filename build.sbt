name := "POSTagger"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies  ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1",
  "org.apache.commons" % "commons-compress" % "1.9",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "org.apache.commons" % "commons-exec" % "1.3"
)

unmanagedBase := baseDirectory.value / "lib"
