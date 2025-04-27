#!/bin/bash

# Configure server.properties
cat <<EOF > /opt/kafka/config/server.properties
broker.id=${KAFKA_BROKER_ID:-1}
zookeeper.connect=${KAFKA_ZOOKEEPER_CONNECT:-zookeeper:2181}
listener.security.protocol.map=PLAINTEXT:PLAINTEXT
advertised.listeners=PLAINTEXT://${KAFKA_ADVERTISED_LISTENERS:-kafka:9092}
num.partitions=1
default.replication.factor=1
offsets.topic.replication.factor=${KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR:-1}
group.initial.rebalance.delay.ms=${KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS:-0}
log.dirs=/kafka/data
EOF

exec "$@"