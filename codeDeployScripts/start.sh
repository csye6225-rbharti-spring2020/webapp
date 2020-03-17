#!/bin/bash

source /etc/profile.d/envvariable.sh
cd /home/ubuntu/myApp/
nohup java -jar ROOT.jar & tail -f nohup.out