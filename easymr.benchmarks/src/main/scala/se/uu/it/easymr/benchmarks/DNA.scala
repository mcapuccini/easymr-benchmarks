package se.uu.it.easymr.benchmarks

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import se.uu.it.easymr.EasyMapReduce

object DNA {

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName("DNA GC count")
    val sc = new SparkContext(conf)

    val fastaRDD = sc.textFile(args(0))
    val res = new EasyMapReduce(fastaRDD)
      .setInputMountPoint("/input.dna")
      .setOutputMountPoint("/output.dna")
      .map(
        imageName = "ubuntu:xenial",
        command = "grep -o '[GC]' /input.dna | wc -l > /output.dna")
      .reduce(
        imageName = "ubuntu:xenial",
        command = "awk '{s+=$1} END {print s}' /input.dna > /output.dna")
    
    println(s"The GC count is: $res")

  }

}
