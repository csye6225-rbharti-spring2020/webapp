#!/bin/bash

cd /home/ubuntu

sudo systemctl start amazon-cloudwatch-agent.service

dir="myApp"

if [ -d $dir ] ; then
    sudo rm -Rf $dir
    sudo mkdir myApp
    cd ..
fi

logsDir="/var/log/aws/codedeploy-agent"

if [ -d $logsDir ] ; then
    cd $logsDir
    sudo rm *.log
    ls -al
    cd ~
fi

#webapplog="/var/log/webapp.log"
#
#if [ -f $webapplog ] ; then
#    cd /var/log/
#    sudo rm webapp.log
#    ls -al
#    sudo touch webapp.log
#    sudo chmod 777 webapp.log
#    cd ~
#fi
