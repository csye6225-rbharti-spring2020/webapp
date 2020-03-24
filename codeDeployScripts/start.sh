#!/bin/bash

sudo chown -R ubuntu:ubuntu /home/ubuntu/*
cd /home/ubuntu/myApp/

sudo chmod +x ROOT.jar

source /etc/profile.d/envvariable.sh

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/myApp/cloudwatch-config.json -s

nohup java -jar ROOT.jar > /home/ubuntu/applog.txt 2> /home/ubuntu/applog.txt < /home/ubuntu/applog.txt &

echo "Waiting for 25 seconds to check the health of the application"
sleep 25