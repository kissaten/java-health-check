#!/usr/bin/env bash

set -o pipefail
set -eu

sleep 60

while true; do
  status=$(curl -sfSL localhost:$PORT/health)

  if [ "$status" != "up" ]; then
    echo "at=health-check status=down"
    kill -9 $(jps | grep -v "Jps" | tail -n1 | grep -o '^\\S*')
  else
    echo "at=health-check status=up"
    sleep 10
  fi
done