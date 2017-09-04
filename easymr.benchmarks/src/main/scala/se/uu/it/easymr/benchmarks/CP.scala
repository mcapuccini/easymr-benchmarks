package se.uu.it.easymr.benchmarks

import java.io.FileOutputStream

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import se.uu.it.easymr.EasyMapReduce
import sun.misc.BASE64Decoder

object CP {

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName("CP sign")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(1 to args(0).toInt, args(1).toInt).map(_.toString)

    val res = new EasyMapReduce(rdd)
      .setOutputMountPoint("/out.txt")
      .setReduceInputMountPoint1("/model1.txt")
      .setReduceInputMountPoint2("/model2.txt")
      .map(
        imageName = "mcapuccini/cpsign",
        command = "java -Xms512m -Xmx8192m -jar cpsign-0.5.7.jar train " +
          "-t data_train.sdf " +
          "-mn out " +
          "-mo /tmp.cpsign " +
          "-c 1 " +
          "--labels 0 1 " +
          "-rn class " +
          "--license cpsign0.5-standard.license && " +
          "[ -e tmp.cpsign ] && " + // workaround for cpsign bug (it always exits with 0)
          "base64 < /tmp.cpsign | tr -d '\n' > /out.txt")
      .reduce(
        imageName = "mcapuccini/cpsign",
        command = "base64 -d < /model1.txt > /model1.cpsign && " +
          "base64 -d < /model2.txt > /model2.cpsign && " +
          "java -Xms512m -Xmx8192m -jar cpsign-0.5.7.jar aggregate " +
          "-m /model1.cpsign /model2.cpsign " +
          "-mn out " +
          "-mo /tmp.cpsign " +
          "-mt 3 " +
          "--license cpsign0.5-standard.license && " +
          "[ -e tmp.cpsign ] && " + // workaround for cpsign bug (it always exits with 0)
          "base64 < /tmp.cpsign | tr -d '\n' > /out.txt")

    // Write model to file
    val base64 = new BASE64Decoder()
    val modelBytes = base64.decodeBuffer(res)
    val fos = new FileOutputStream("model.cpsign")
    fos.write(modelBytes)
    fos.close()

    // Stop Spark
    sc.stop

  }

}