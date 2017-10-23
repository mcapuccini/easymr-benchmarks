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

  // Fixing random seeds to compare with serial training
  val seeds = Seq(
      2200, 7872, 1935, 3120, 3723, 6791, 6244, 7278, 3058, 1465, 
      2986, 9136, 3465, 2453, 8290, 4438, 4020, 1356, 3779, 5965, 
      5170, 2486, 4411, 8325, 7623, 3151, 7935, 1520, 2191, 2638, 
      1200, 2061, 6602, 5971, 1422, 6021, 2677, 3478, 6123, 8957, 
      2010, 1220, 7410, 5776, 1251, 7002, 2742, 6092, 1912, 6547, 
      8473, 3477, 3709, 4421, 9830, 3398, 5129, 7782, 8370, 3166, 
      4726, 6767, 4458, 5676, 5708, 1656, 2076, 9496, 3923, 4416, 
      5770, 1715, 2434, 6470, 7350, 1894, 2142, 5726, 3147, 9882, 
      9855, 7206, 6553, 7340, 1218, 7717, 1766, 6573, 7131, 4484, 
      4867, 4281, 2174, 3990, 8124, 9387, 6062, 3466, 7329, 1921)

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName("CP sign")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(seeds.map(_.toString), args(0).toInt)

    // Define median primitive
    val median = (seq: Seq[Double]) => if (seq.length % 2 == 0) {
      val sort = seq.sortWith(_ < _)
      (sort(sort.length / 2) + sort(sort.length / 2 - 1)) / 2
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
          "--seed $(cat /in.txt | tr -d '\n') " +
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
        val rpv0 = BigDecimal(pv0).setScale(3, BigDecimal.RoundingMode.HALF_UP).toString
        val rpv1 = BigDecimal(pv1).setScale(3, BigDecimal.RoundingMode.HALF_UP).toString
        pw.println(s"${title},${rpv0},${rpv1}")
    }
    pw.close

    // Stop Spark
    sc.stop

  }

}