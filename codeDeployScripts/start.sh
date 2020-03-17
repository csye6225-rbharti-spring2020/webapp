#!/bin/bash

cd /home/ubuntu/myApp/

sudo chown -R ubuntu:ubuntu /home/ubuntu/myApp/*
sudo chmod +x ROOT.jar
java -jar ROOT.jar

source /etc/profile.d/envvariable.sh
nohup java -jar ROOT.jar > /home/ubuntu/log.txt 2> /home/ubuntu/log.txt < /home/ubuntu/log.txt &