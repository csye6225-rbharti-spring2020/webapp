#!/bin/bash

cd /home/ubuntu/myApp/

sudo chown -R ubuntu:ubuntu /home/ubuntu/myApp/*
sudo chmod +x cloudproject-0.0.1-SNAPSHOT.jar

source /etc/profile.d/envvariable.sh
nohup java -jar cloudproject-0.0.1-SNAPSHOT.jar > /home/ubuntu/applog.txt 2> /home/ubuntu/applog.txt < /home/ubuntu/applog.txt &