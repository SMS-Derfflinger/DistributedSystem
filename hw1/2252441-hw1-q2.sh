#!/usr/bin/bash
touch 2252441-hw1-q2.log
for (( i=0; i<20; i++ )); do
    uptime | awk '{$1=$1; print}' >> 2252441-hw1-q2.log
    sleep 10
done
