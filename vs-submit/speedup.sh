#!/bin/bash

for cpu in 120 100 80 60 40 20 ; do
 CPU=$cpu ./submit.sh
done

