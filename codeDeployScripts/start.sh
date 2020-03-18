#!/bin/bash

cd /home/ubuntu/myApp/

sudo chown -R ubuntu:ubuntu /home/ubuntu/myApp/*
sudo chmod +x ROOT.jar

source /etc/profile.d/envvariable.sh

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/myApp/cloudwatch-config.json -s

nohup java -jar ROOT.jar > /home/ubuntu/applog.txt 2> /home/ubuntu/applog.txt < /home/ubuntu/applog.txt &