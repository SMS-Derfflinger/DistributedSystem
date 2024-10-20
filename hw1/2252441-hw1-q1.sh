#!/usr/bin/bash
for num in {1..100}; do
    is_prime=1
    for (( i=2; i*i<=$num; i++ )); do
        if [ $((num % i)) -eq 0 ]; then
            is_prime=0
            break
        fi
    done
    if [ $is_prime -eq 1 ]; then
        echo "$num" >> 2252441-hw1-q1.log
    fi
done
