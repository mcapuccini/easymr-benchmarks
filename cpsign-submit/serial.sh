#!/bin/bash

seeds="2200 7872 1935 3120 3723 6791 6244 7278 3058 1465 2986 9136 3465 2453 8290 4438 4020 1356 3779 5965 5170 2486 4411 8325 7623 3151 7935 1520 2191 2638 1200 2061 6602 5971 1422 6021 2677 3478 6123 8957 2010 1220 7410 5776 1251 7002 2742 6092 1912 6547 8473 3477 3709 4421 9830 3398 5129 7782 8370 3166 4726 6767 4458 5676 5708 1656 2076 9496 3923 4416 5770 1715 2434 6470 7350 1894 2142 5726 3147 9882 9855 7206 6553 7340 1218 7717 1766 6573 7131 4484 4867 4281 2174 3990 8124 9387 6062 3466 7329 1921"

for s in $seeds; do
  echo "Training with seed $s"
  java -jar cpsign-0.6.1.jar train \
    -t data_train.sdf \
    -mn out_${s} \
    -mo /model_${s}.cpsign \
    -c 1 \
    --labels 0 1 \
    -rn class \
    --seed $s \
    --license cpsign0.6-standard.license 
done

echo "Aggregating models"
java -jar cpsign-0.6.1.jar fast-aggregate \
  -m /model_*.cpsign \
  -mo /models.cpsign \
  --license cpsign0.6-standard.license

echo "Performing predictions"
  java -jar cpsign-0.6.1.jar predict \
  -m /models.cpsign \
  -p data_test.sdf \
  -c 1 \
  -co 0.8 \
  -of plain \
  --license cpsign0.6-standard.license
