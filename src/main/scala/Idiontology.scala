import java.io.{File, PrintWriter}

import org.neo4j.cypher.ExecutionEngine
import org.neo4j.cypher.ExecutionResult
import org.neo4j.graphdb._
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.impl.util.StringLogger

import scala.collection.mutable
import scala.collection.par._
import scala.collection.par.Scheduler.Implicits.global

import spray.json._
import DefaultJsonProtocol._


/**
 * Created by dav009 on 15/09/2014.
 */
class Idiontology(val pathToDB: String) {

  val  db:GraphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase( pathToDB )
  val autoIndex = db.index().getNodeAutoIndexer.getAutoIndex
  val engine:ExecutionEngine = new ExecutionEngine( db, StringLogger.SYSTEM );



  db

  def getName(mid:String): String ={
    try {
      val typeNode: Node = autoIndex.get("mid", mid).getSingle
       typeNode.getProperty("name").toString
    }catch{


      case e:Exception => {

        e.printStackTrace()
        ""
      }
    }


  }

  def getNode(mid:String):Node={
      autoIndex.get("mid", mid).getSingle()

  }


  def getTypeRels(pathToOutputFile: String): Unit ={

    // write them to file
    val writer = new PrintWriter(new File(pathToOutputFile))

    val cypherQuery :String =
      """
        START root=node:node_auto_index(mid="/m/idiothing")
        MATCH root -[r:domain_rel]-> fb_domain
        with fb_domain MATCH fb_domain -[s:domain_rel]-> fb_type
        with fb_type MATCH fb_type -[v:domain_rel]-> fb_sub_type
        with fb_sub_type MATCH fb_sub_type -[x:type_rel]-> t_node
        where HAS(t_node.dbpedia_id) and t_node.dbpedia_id IS NOT NULL and not t_node.dbpedia_id="__NULL__"
        return fb_sub_type.mid as type_id, t_node.mid as topic_mid, t_node.dbpedia_id as topic_dbpedia, ID(x) as rel_id

      """
    val tx:Transaction = db.beginTx();
    try{
      val result : ExecutionResult= engine.execute(cypherQuery );



      result.foreach{
        row: Map[String, Any] =>

          val map =row.asInstanceOf[Map[String, Any]]

          val rel_id = map.get("rel_id").get.toString
          val node_mid = map.get("topic_mid").get.toString
          val type_mid = map.get("type_id").get.toString

          writer.write(rel_id + "\t" + node_mid +"\t"  + "\t" + type_mid +"\t"+"\n")
      }
    }

    finally{
      tx.finish()
    }


    writer.close()
  }

  def setWeights(weights:mutable.ArraySeq[(String, Double)]): Unit = {

    println("inserting into neo4j...")

    var counter = 0

      weights.foreach{

        case(relationshipId:String, weight:Double) =>
          counter = counter + 1
          val tx:Transaction = db.beginTx();
          println(counter+ "..")
          try{

            db.getRelationshipById(relationshipId.toLong).setProperty("weight", weight)
            tx.success();
          }catch{
            case e:Exception=> e.printStackTrace()
          }
          finally{
            tx.finish()
          }
      }




  }


}



object CompleteFile{


  def main(args:Array[String]): Unit ={

      val pathToFile = args(0)
      val pathToIdiontology = args(1)
      val outputFile = pathToFile + "_with_names"
      val fileLines = scala.io.Source.fromFile(pathToFile).getLines().toList

      val idiontology = new Idiontology(pathToIdiontology)
      val writer = new PrintWriter(new File(outputFile))



     fileLines.foreach{

       line:String =>
          val splitLine = line.trim().split("\t")
          val typeMid = splitLine(3)
           val name = idiontology.getName(typeMid)
         writer.write(splitLine.mkString("\t") +"\t" +name+ "\n" )

     }

    writer.close()


  }

}


object exportRelationships{

  def main(args: Array[String]): Unit = {
    val outputFile = args(0)
    val pathToIdiontology = args(1)
    val idiontology = new Idiontology(pathToIdiontology)
    idiontology.getTypeRels(outputFile)
  }
}


object LoadRelationshipsWeights{



  def main(args: Array[String]): Unit ={

    val pathToFileWithWeights = args(0)
    val pathToIdiontology = args(1)
    val weightFile = scala.io.Source.fromFile(pathToFileWithWeights).getLines().toList

    val idiontology = new Idiontology(pathToIdiontology)
    var lineNumber =0
    println("setting weights map")
    val relationshipWeightMap = weightFile.toParArray.map{
       line:String =>
         lineNumber = lineNumber +1
           println(lineNumber)
           val splitLine = line.trim().split("\t")

           val weight = splitLine(0).toDouble
           val topicMid = splitLine(1)
           val typeMid = splitLine(3)
           val relId = splitLine(4)

         (relId, weight)
    }

    idiontology.setWeights(relationshipWeightMap.seq)
    println("finished setting weights map")

  }

}

object MergeScoresAndRelationshipsIds{

  def main(args: Array[String]): Unit = {
    val pathToFileWithWeights = args(0)
    val pathToFileWithIds = args(1)
    val outputFile = args(2)
    val weightFile = scala.io.Source.fromFile(pathToFileWithWeights).getLines()
    val fileWithIds = scala.io.Source.fromFile(pathToFileWithIds).getLines().toList
    val writer = new PrintWriter(new File(outputFile))

    var lineNumber = 0
    println("creating map")
    val midsToRelId = fileWithIds.toParArray.map {
      line: String =>
        lineNumber = lineNumber + 1
        println(lineNumber)
        val splitLine = line.trim().split("\t")

        val relId = splitLine(0)
        val topicMid = splitLine(1)
        val typeMid = splitLine(3)


        (topicMid + "_" + typeMid) -> relId
    }.seq.toMap

    println("finished creating map")

    println("exporting file with ids..")
    var counter = 0
    weightFile.foreach{
       line :String =>
         counter  = counter + 1
         println(counter + "..")
          val splitLine = line.trim().split("\t")

          val topicMid = splitLine(1)
          val typeMid = splitLine(3)

          val relId = midsToRelId.get(topicMid + "_" + typeMid).get

         writer.write(line.trim()+"\t"+relId+"\n")



    }

    println("finished exporting file with ids..")

    writer.close()


}


}
