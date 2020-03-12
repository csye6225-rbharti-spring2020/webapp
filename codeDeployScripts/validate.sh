#!/bin/bash

echo "Waiting for 20 seconds to check the health of the application"
sleep 20

res=`curl -s --head http://localhost:8080/actuator/health | head -n 1 | grep -c HTTP/1.1 200 OK`

if [ $res -eq 1 ]
then
MSG = " OKAY"
exit 0
else
MSG = " NOT OKAY"
exit 1
fi