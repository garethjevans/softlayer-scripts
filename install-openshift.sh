#!/bin/sh
LOG=/tmp/install.log

yum -y update > $LOG
yum -y install net-tools ntp >> $LOG

systemctl start ntpd >> $LOG
systemctl enable ntpd >> $LOG

systemctl stop firewalld >> $LOG
systemctl disable firewalld >> $LOG

yum -y install docker >> $LOG

echo "Downloading Openshift" >> $LOG
curl -L https://github.com/openshift/origin/releases/download/v0.6/openshift-origin-v0.6-e456d58-linux-amd64.tar.gz | tar xzv

echo "Done" >> $LOG


