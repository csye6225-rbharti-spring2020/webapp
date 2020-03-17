#!/bin/bash

cd /home/ubuntu/myApp/

sudo chown -R ubuntu:ubuntu /home/ubuntu/myApp/*
sudo chmod +x ROOT.jar

source /etc/profile.d/envvariable.sh
nohup java -jar -Dspring.profiles.active=${appProfile} ROOT.jar > /home/ubuntu/myApp/log.txt 2> /home/ubuntu/myApp/log.txt < /home/ubuntu/myApp/log.txt &