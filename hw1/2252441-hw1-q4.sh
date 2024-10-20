#!/usr/bin/bash
countdown=$1
while [ $countdown -gt 0 ]; do
    if [ $((countdown % 10)) -eq 0 ]; then
        echo "剩余时间: $countdown 秒"
    fi
    sleep 1
    countdown=$((countdown - 1))
done

echo "倒计时结束！"