#!/bin/bash

JAR=~/easymr-benchmarks/easymr.benchmarks/target/easymr.benchmarks-0.0.1-SNAPSHOT-jar-with-dependencies.jar
export TMPDIR=/tmp/ram
SAMPLE=1

/opt/spark/default/bin/spark-submit \
  --class se.uu.it.easymr.benchmarks.VS \
  --master spark://spark-master.node.dc1.consul:7077 \
  --executor-memory 2G \
  --conf spark.driver.memory=2G \
  --total-executor-cores $CPU \
  --conf spark.executorEnv.TMPDIR=/tmp/ram \
  $JAR \
  'hdfs://spark-master.node.dc1.consul:9000/ubuntu/*.sdf' \
  $SAMPLE

