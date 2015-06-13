#!/bin/sh

yum -y update
yum -y install nettools

cat << EOF > /etc/yum.repos.d/virt7-testing.repo
[virt7-testing]
name=virt7-testing
baseurl=http://cbs.centos.org/repos/virt7-testing/x86_64/os/
gpgcheck=0
EOF

yum -y install --enablerepo=virt7-testing kubernetes etcd

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
