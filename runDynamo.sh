#!/bin/sh

for i in `seq 0 99`
do
    nohup java -jar Final.jar $i 2000 100 5 25 &
    #nohup java -Xmx256m -jar FinalGossip.jar $i 2000 100 20 ORIGINAL &
done
