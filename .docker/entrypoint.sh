#!/usr/bin/env bash

devices=$((DEVICE_NUM))
current_host=$HOST

if [[ "$DEVICE_TYPE" == "smartphone" ]]; then
  ./smartphone.sh $devices "$current_host"
elif [[ "$DEVICE_TYPE" == "room" ]]; then
  ./room.sh
else
  echo "Unknown device type"
  exit 255
fi
