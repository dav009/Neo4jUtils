import _root_.sbt.Keys._

packAutoSettings

name := "Neo4jUtilities"

libraryDependencies += "org.neo4j" % "neo4j-kernel" % "1.9.8"

libraryDependencies += "org.neo4j" % "neo4j-lucene-index" % "1.9.8"

libraryDependencies += "org.neo4j" % "neo4j" % "1.9.8"

version := "1.0"
