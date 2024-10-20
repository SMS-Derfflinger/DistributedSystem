#!/usr/bin/bash
filename=$1
line_count=$(wc -l $filename | cut -d ' ' -f 1)
char_count=$(wc -m $filename | cut -d ' ' -f 1)
first_time=$(sed -n '1p' $filename | cut -d ' ' -f 1)
last_time=$(sed '/^$/d' $filename | tail -n 1 | cut -d ' ' -f 1)
diff=`expr $(date -d "$last_time" +%s) - $(date -d "$first_time" +%s)`
load1=0
load5=0
load15=0
for(( i=1; i<=line_count; i++)); do
    read_line=$(head -n $i $filename | tail -n 1 )
    read avg1 avg5 avg15 <<< $(echo $read_line | awk -F'load average: ' '{print $2}' | awk -F', ' '{print $1, $2, $3}')
    load1=$(echo "$load1 + $avg1" | bc)
    load5=$(echo "$load5 + $avg5" | bc)
    load15=$(echo "$load15 + $avg15" | bc)
done
    load1=$(echo "scale=2;$load1 / $line_count" | bc)
    load5=$(echo "scale=2;$load5 / $line_count" | bc)
    load15=$(echo "scale=2;$load15 / $line_count" | bc)
    echo -e "$line_count\n$char_count\n$diff\n$load1 $load5 $load15" > 2252441-hw1-q3.log
