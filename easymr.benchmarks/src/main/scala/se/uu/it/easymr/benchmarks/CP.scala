package se.uu.it.easymr.benchmarks

import java.io.PrintWriter

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.json4s.jvalue2monadic
import org.json4s.native.JsonMethods.compact
import org.json4s.native.JsonMethods.parse
import org.json4s.native.JsonMethods.render
import org.json4s.string2JsonInput

import se.uu.it.easymr.EasyMapReduce

object CP {

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName("CP sign")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(1 to args(0).toInt, args(1).toInt).map(_.toString)

    // Define median primitive
    val median = (seq: Seq[Double]) => if (seq.length % 2 == 0) {
      (seq(seq.length / 2) + seq(seq.length / 2 - 1)) / 2
    } else {
      seq.sortWith(_ < _)(seq.length / 2)
    }

    val predictions = new EasyMapReduce(rdd)
      .setInputMountPoint("/in.txt")
      .setOutputMountPoint("/out.txt")
      // Train
      .map(
        imageName = "mcapuccini/cpsign",
        command = "java -jar cpsign-0.6.1.jar train " +
          "-t data_train.sdf " +
          "-mn out " +
          "-mo /tmp.cpsign " +
          "-c 1 " +
          "--labels 0 1 " +
          "-rn class " +
          "--license cpsign0.6-standard.license && " +
          "[ -e tmp.cpsign ] && " + // workaround for cpsign bug (it always exits with 0)
          "base64 < /tmp.cpsign | tr -d '\n' > /out.txt")
      // Predict
      .map(
        imageName = "mcapuccini/cpsign",
        command = "base64 -d < /in.txt > /model.cpsign && " +
          "java -jar cpsign-0.6.1.jar predict " +
          "-m /model.cpsign " +
          "-p data_test.sdf " +
          "-c 1 " +
          "-co 0.8 " +
          "-o /out.txt " +
          "--license cpsign0.6-standard.license")
      .getRDD.map { json =>
        val parsedJson = parse(json)
        val key = compact(render(parsedJson \ "molecule" \ "n"))
        val pv0 = compact(render(parsedJson \ "prediction" \ "pValues" \ "0")).toDouble
        val pv1 = compact(render(parsedJson \ "prediction" \ "pValues" \ "1")).toDouble
        (key, (Seq(pv0), Seq(pv1)))
      }
      .reduceByKey { case ((seq0a, seq1a), (seq0b, seq1b)) => (seq0a ++ seq0b, seq1a ++ seq1b) }
      .map { case (key, (s0, s1)) => (key, median(s0), median(s1)) }

    // Write in CSV format
    val pw = new PrintWriter("predictions.csv")
    predictions.collect.foreach {
      case (title, pv0, pv1) => 
          val rpv0 = BigDecimal(pv0).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
          val rpv1 = BigDecimal(pv1).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
          pw.println(s"${title},${pv0},${pv1}")
    }
    pw.close

    // Stop Spark
    sc.stop

  }

}