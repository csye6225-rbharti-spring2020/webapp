#!/bin/bash
#Commands to run after after installation
echo "Entered after install hook"
cd /home/ubuntu/webapp
sudo chown -R ubuntu:ubuntu /home/ubuntu/webapp/*
sudo chmod +x cloudproject-0.0.1-SNAPSHOT.jar

#Killing the application
kill -9 $(ps -ef|grep cloudproject-0.0.1 | grep -v grep | awk '{print $2}')

source /etc/profile.d/envvariable.sh
nohup java -jar cloudproject-0.0.1-SNAPSHOT.jar > /home/ubuntu/log.txt 2> /home/ubuntu/log.txt < /home/ubuntu/log.txt &