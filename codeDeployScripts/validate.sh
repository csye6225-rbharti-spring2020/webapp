#!/bin/bash

for i in `seq 1 10`;
do
  HTTP_CODE=`curl --write-out '%{http_code}' -o /dev/null -m 10 -q -s https://localhost:8080/actuator/health`
  if [ "$HTTP_CODE" == "200" ]; then
    echo "Actuator Health Endpoint Worked Successfully - $response_code"
    exit 0;
  fi
  echo "Attempt to curl endpoint returned HTTP Code $HTTP_CODE. Backing off and retrying."
  sleep 10
done
echo "Server did not come up after expected time. Failing."
exit 1