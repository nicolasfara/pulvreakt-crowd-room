#!/usr/bin/env bash

if [[ $# -lt 2 ]]
then
  echo "usage: ./entrypoint <num-devices> <host>"
  exit 255
fi

num_devices=$(($1))
host="$2"

until nc -z "${RABBITMQ_HOST:-rabbitmq}" "${RABBITMQ_PORT:-5672}"; do
  echo "$(date) - waiting for rabbitmq..."
  sleep 5
done

for ((i=1;i<=num_devices;i++))
do
  java -jar wearable-all.jar "$((i))" "$host" &
  pids[$i]=$!
done

# Join spawned process
for pid in ${pids[*]}; do
  wait $pid
done
