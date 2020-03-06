#!/usr/bin/env bash

echo "Waiting for 10 seconds to check the health of the application"
sleep 10

response_code=$(sudo curl --write-out %{http_code} --silent --output /dev/null http://localhost:8080/actuator/health)

if [[ "$response_code" -ne 200 ]] ; then
  echo "Actuator Health Endpoint Worked Successfully - $response_code"
  exit 1
else
  echo "The application is responding with $response_code"
  exit 0
fi