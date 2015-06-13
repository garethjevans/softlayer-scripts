#!/bin/sh

yum -y update
yum -y install net-tools ntp

systemctl start ntpd
systemctl enable ntpd

yum -y install kubernetes etcd

cat << EOF > start-master.sh
#!/bin/sh

for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler; do
    systemctl restart \$SERVICES
    systemctl enable \$SERVICES
    systemctl status \$SERVICES
done
EOF
chmod +x start-master.sh

cat << EOF > stop-master.sh
#!/bin/sh

for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler; do
    systemctl stop \$SERVICES
done
EOF
chmod +x stop-master.sh
