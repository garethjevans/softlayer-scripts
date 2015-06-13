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
curl -L https://github.com/openshift/origin/releases/download/v0.5.1/openshift-origin-v0.5.1-ce1e6c4-linux-amd64.tar.gz | tar xzv

echo "Done" >> $LOG


