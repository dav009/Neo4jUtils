import _root_.sbt.Keys._

name := "Neo4jUtilities"


resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "org.neo4j" % "neo4j-kernel" % "1.9.8"

libraryDependencies += "org.neo4j" % "neo4j-lucene-index" % "1.9.8"

libraryDependencies += "org.neo4j" % "neo4j" % "1.9.8"

libraryDependencies += "com.github.scala-blitz" %% "scala-blitz" % "1.0-M1"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.6"

version := "1.0"
