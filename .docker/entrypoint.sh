#!/usr/bin/env bash

devices=$((DEVICE_NUM))
current_host=$HOST

if [[ "$DEVICE_TYPE" == "wearable" ]]; then
  ./wearable.sh $devices "$current_host"
elif [[ "$DEVICE_TYPE" == "laboratory" ]]; then
  ./laboratory.sh
else
  echo "Unknown device type"
  exit 255
fi
