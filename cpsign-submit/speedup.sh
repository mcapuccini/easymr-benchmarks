#!/bin/bash

for cpu in 10 20 30 40 ; do
 CPU=$cpu NCP=100 NSPL=40 ./submit.sh
done

