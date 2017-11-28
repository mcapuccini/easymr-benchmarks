package se.uu.it.easymr.benchmarks

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import se.uu.it.easymr.EasyMapReduce

object VS {

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName("Virtual Screening")
    val sc = new SparkContext(conf)
    sc.addJar(args(2)) // add swift integration Jar to each node
    
    // Set custom delimiter
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", "\n$$$$\n")

    val mols = sc.textFile(args(0)).sample(false, args(1).toDouble)
    val hitsParallel = new EasyMapReduce(mols)
      .setInputMountPoint("/input.sdf")
      .setOutputMountPoint("/output.sdf")
      .mapPartitions(
        imageName = "mcapuccini/oe-docking", // obs: this is a private image
        command = "fred -receptor /var/openeye/hiv1_protease.oeb " +
          "-hitlist_size 0 " +
          "-conftest none " +
          "-dbase /input.sdf " +
          "-docked_molecule_file /output.sdf")
      .reducePartitions(
        imageName = "mcapuccini/sdsorter",
        command = "sdsorter -reversesort='FRED Chemgauss4 score' " +
          "-keep-tag='FRED Chemgauss4 score' " +
          "-nbest=3 " +
          "/input.sdf " +
          "/output.sdf")
    
    println(hitsParallel)

  }

}
