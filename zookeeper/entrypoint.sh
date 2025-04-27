#!/bin/bash

# Configure zoo.cfg
cat <<EOF > /opt/zookeeper/conf/zoo.cfg
tickTime=${ZOOKEEPER_TICK_TIME:-2000}
dataDir=/zookeeper/data
clientPort=${ZOOKEEPER_CLIENT_PORT:-2181}
maxClientCnxns=60
initLimit=10
syncLimit=5
EOF

# Set Zookeeper ID
echo "1" > /zookeeper/data/myid

exec "$@"