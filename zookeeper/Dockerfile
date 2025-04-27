FROM openjdk:17-jdk-slim

ENV ZOOKEEPER_VERSION=3.8.0
ENV ZOOKEEPER_HOME=/opt/zookeeper

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    tar \
    findutils \
    && rm -rf /var/lib/apt/lists/*

# Download and install Zookeeper
RUN wget https://archive.apache.org/dist/zookeeper/zookeeper-${ZOOKEEPER_VERSION}/apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz \
    && tar -xzf apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz -C /opt \
    && mv /opt/apache-zookeeper-${ZOOKEEPER_VERSION}-bin ${ZOOKEEPER_HOME} \
    && rm apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz

# Add Zookeeper binaries to PATH
ENV PATH=${ZOOKEEPER_HOME}/bin:${PATH}

# Create Zookeeper data directory
RUN mkdir -p /zookeeper/data /zookeeper/logs

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["zkServer.sh", "start-foreground"]