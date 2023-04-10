#!/bin/bash

: "${MIN_MEMORY:=256M}}"
: "${MAX_MEMORY:=4G}}"

echo "[init] Copying HyriBot jar"
cp /usr/app/HyriBot.jar /data

echo "[init] Starting process..."
exec java -Xms${MIN_MEMORY} -Xmx${MAX_MEMORY} -jar HyriBot.jar