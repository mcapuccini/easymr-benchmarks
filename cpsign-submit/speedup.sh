#!/bin/bash

for cpu in 40 30 20 10 ; do
 CPU=$cpu NSPL=40 ./submit.sh
 mv predictions.csv predictions_${cpu}.csv
done

