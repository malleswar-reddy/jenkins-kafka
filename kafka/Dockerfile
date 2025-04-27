```dockerfile
FROM openjdk:17-jdk-slim

ENV KAFKA_VERSION=3.3.1
ENV SCALA_VERSION=2.13
ENV KAFKA_HOME=/opt/kafka

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    tar \
    findutils \
    && rm -rf /var/lib/apt/lists/*

# Download and install Kafka
RUN wget https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz \
    && tar -xzf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt \
    && mv /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} ${KAFKA_HOME} \
    && rm kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz

# Add Kafka binaries to PATH
ENV PATH=${KAFKA_HOME}/bin:${PATH}

# Create Kafka data directory
RUN mkdir -p /kafka/data /kafka/logs

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["kafka-server-start.sh", "/opt/kafka/config/server.properties"]
```