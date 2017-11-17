#!/bin/bash

for cpu in 100 80 60 40 20; do
 CPU=$cpu NSPL=100 ./submit.sh
 mv predictions.csv predictions_${cpu}.csv
done

