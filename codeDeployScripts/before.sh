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
