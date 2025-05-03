```markdown
# Jenkins-Kafka Application

This project is a Kafka-based microservices application with three services: `OrderService`, `StockService`, and `NotificationService`. It uses Apache Kafka for messaging, Zookeeper for coordination, and Docker for containerization. The application processes orders published to the `order_topics` topic, consumed by `StockService` (consumer group: `stock-group`) and `NotificationService` (consumer group: `notification-group`). Invalid messages are sent to the `order_topics-dlt` dead-letter topic.

## Project Structure
- **OrderService**: Publishes orders to `order_topics` via HTTP POST to `http://localhost:8081/orders`.
- **StockService**: Consumes orders from `order_topics`, processes stock updates.
- **NotificationService**: Consumes orders from `order_topics`, sends notifications.
- **Kafka**: Broker for messaging (`kafka:9092`).
- **Zookeeper**: Coordination service (`zookeeper:2181`).

## Prerequisites
- Docker and Docker Compose
- Maven
- Java 17
- Postman (for testing)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd jenkins-kafka
```

### 2. Build Custom Kafka and Zookeeper Images
To ensure CLI tools (`kafka-topics.sh`, `kafka-console-consumer.sh`) are available, use custom images.

#### Kafka Image
In `kafka/`:
```bash
mkdir -p kafka
cd kafka
```

**`kafka/Dockerfile`**:
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

**`kafka/entrypoint.sh`**:
```bash
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
```

Build:
```bash
docker build -t custom-kafka:3.3.1 .
cd ..
```

#### Zookeeper Image
In `zookeeper/`:
```bash
mkdir -p zookeeper
cd zookeeper
```

**`zookeeper/Dockerfile`**:
```dockerfile
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
```

**`zookeeper/entrypoint.sh`**:
```bash
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
```

Build:
```bash
docker build -t custom-zookeeper:3.8.0 .
cd ..
```

### 3. Configure Docker Compose
Use the following `docker-compose.yml`:
```yaml
version: '3.8'
services:
  zookeeper:
    image: custom-zookeeper:3.8.0
    container_name: jenkins-kafka-zookeeper-1
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    volumes:
      - zookeeper_data:/zookeeper/data
      - zookeeper_logs:/zookeeper/logs
  kafka:
    image: custom-kafka:3.3.1
    container_name: jenkins-kafka-kafka-1
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    ports:
      - "9092:9092"
    volumes:
      - kafka_data:/kafka/data
      - kafka_logs:/kafka/logs
  order-service:
    image: jenkins-kafka-order-service-1
    container_name: jenkins-kafka-order-service-1
    build:
      context: ./order-service
      dockerfile: Dockerfile
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8081:8081"
  stock-service:
    image: jenkins-kafka-stock-service-1
    container_name: jenkins-kafka-stock-service-1
    build:
      context: ./stock-service
      dockerfile: Dockerfile
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8082:8082"
  notification-service:
    image: jenkins-kafka-notification-service-1
    container_name: jenkins-kafka-notification-service-1
    build:
      context: ./notification-service
      dockerfile: Dockerfile
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8083:8083"
volumes:
  zookeeper_data:
  zookeeper_logs:
  kafka_data:
  kafka_logs:
```

Save to `docker-compose.yml`.

### 4. Build Services
```bash
cd order-service
mvn clean package
cd ../stock-service
mvn clean package
cd ../notification-service
mvn clean package
cd ..
```

### 5. Start Services
```bash
docker-compose up -d

docker compose up -d
```

Verify:
```bash
docker ps
```

### 6. Create Kafka Topics
```copy
docker exec -it jenkins-kafka-kafka-1 kafka-topics --bootstrap-server kafka:9092 --create --topic order_topics --partitions 1 --replication-factor 1
docker exec -it jenkins-kafka-kafka-1 kafka-topics --bootstrap-server kafka:9092 --create --topic order_topics-dlt --partitions 1 --replication-factor 1

```

Verify:
```copy
  docker exec -it jenkins-kafka-kafka-1 kafka-topics --bootstrap-server kafka:9092 --list
```

## Key Configurations

### OrderService Producer
In `order-service/src/main/resources/application.yml`:
```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: kafka:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        enable.idempotence: true
        acks: all
        retries: 3
        max.in.flight.requests.per.connection: 1
```

### StockService and NotificationService Consumers
In `stock-service/src/main/resources/application.yml` and `notification-service/src/main/resources/application.yml`:
```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: kafka:9092
      group-id: stock-group  # or notification-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: 'com.ecommerce.commonservice.*'
```

### DeadLetterPublishingRecoverer
In `StockService` and `NotificationService`:
```java
@Bean
public DefaultErrorHandler errorHandler() {
    KafkaTemplate<String, Order> kafkaTemplate = kafkaTemplate();
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition("order_topics-dlt", record.partition()));
    return new DefaultErrorHandler(recoverer);
}
```

## Testing Instructions

1. **Send Test Order**:
   Use Postman to POST to `http://localhost:8081/orders`:
   ```json
   {
       "orderId": "ORD022",
       "productId": "PROD022",
       "quantity": 14
   }
   ```

2. **Verify Logs**:
   ```bash
   docker logs jenkins-kafka-order-service-1
   docker logs jenkins-kafka-stock-service-1
   docker logs jenkins-kafka-notification-service-1
   ```

   Expected:
   - `OrderService`: `Published order ORD022 to order_topics`
   - `StockService`: `Processing order: ORD022, Product: PROD022, Quantity: 14`
   - `NotificationService`: `Sending notification for order: ORD022`

3. **Inspect Topics**:
   ```bash
   docker exec -it jenkins-kafka-kafka-1 kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics --from-beginning
   docker exec -it jenkins-kafka-kafka-1 kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics-dlt --from-beginning
   ```

4. **Check Consumer Groups**:
   ```bash
   docker exec -it jenkins-kafka-kafka-1 kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group stock-group
   docker exec -it jenkins-kafka-kafka-1 kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group notification-group
   ```

## Troubleshooting Guide

### 1. Kafka Fails to Start
**Symptoms**:
- `docker ps -a` shows `jenkins-kafka-kafka-1` as `Exited`.
- Logs show `IllegalArgumentException: Error creating broker listeners` or `NodeExistsException`.

**Resolution**:
- **Invalid `KAFKA_ADVERTISED_LISTENERS`**:
  - Ensure `KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092` in `docker-compose.yml`.
  - Fix: Update `docker-compose.yml`, then:
    ```bash
    docker-compose down
    docker-compose up -d --build
    
    docker compose down
    docker compose up -d --build
    ```

- **NodeExistsException**:
  - Clear Zookeeper data:
    ```bash
    docker-compose down
    docker volume rm jenkins-kafka_zookeeper_data jenkins-kafka_kafka_data
    docker-compose up -d
    ```
  - Or manually delete broker:
    ```bash
    docker exec -it jenkins-kafka-zookeeper-1 zookeeper-shell zookeeper:2181
    ls /brokers/ids
    delete /brokers/ids/1
    quit
    ```

- **Check Logs**:
  ```bash
  docker logs jenkins-kafka-kafka-1
  ```

### 2. Missing CLI Tools
**Symptoms**:
- `kafka-topics.sh` or `kafka-console-consumer.sh` not found in `jenkins-kafka-kafka-1`.

**Resolution**:
- Use custom Kafka image (`custom-kafka:3.3.1`) as described in Setup.
- Verify:
  ```bash
  docker exec -it jenkins-kafka-kafka-1 ls -l /opt/kafka/bin/
  ```

### 3. OrderService Fails
**Symptoms**:
- `jenkins-kafka-order-service-1` is `Exited`.
- Logs show `KafkaException` or `Connection refused`.

**Resolution**:
- Ensure Kafka is running:
  ```bash
  docker ps
  docker logs jenkins-kafka-kafka-1
  ```
- Check `application.yml`:
  ```yaml
  spring:
    kafka:
      producer:
        bootstrap-servers: kafka:9092
  ```
- Rebuild:
  ```bash
  cd order-service
  mvn clean package
  cd ..
  docker-compose up -d --build order-service
  ```

### 4. Duplicate Messages
**Symptoms**:
- Multiple identical messages in `order_topics` (e.g., `{"orderId":"ORD001","productId":"PROD001","quantity":2}`).

**Resolution**:
- Enable idempotent producer in `order-service/src/main/resources/application.yml`:
  ```yaml
  spring:
    kafka:
      producer:
        properties:
          enable.idempotence: true
          acks: all
          retries: 3
          max.in.flight.requests.per.connection: 1
  ```
- Rebuild:
  ```bash
  cd order-service
  mvn clean package
  cd ..
  docker-compose up -d --build order-service
  ```

### 5. SerializationException
**Symptoms**:
- Messages in `order_topics-dlt`.
- Logs show `SerializationException`.

**Resolution**:
- Ensure `JsonSerializer`/`JsonDeserializer` in `application.yml`:
  ```yaml
  spring:
    kafka:
      producer:
        value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      consumer:
        value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
        properties:
          spring.json.trusted.packages: 'com.ecommerce.commonservice.*'
  ```
- Validate `productId` in `StockService` and `NotificationService` to handle invalid values (e.g., `HHHHHSee`).
- Rebuild:
  ```bash
  cd stock-service
  mvn clean package
  cd ../notification-service
  mvn clean package
  cd ..
  docker-compose up -d --build
  ```

### 6. High Consumer Lag
**Symptoms**:
- `kafka-consumer-groups.sh` shows high `LAG`.

**Resolution**:
- Check consumer logs:
  ```bash
  docker logs jenkins-kafka-stock-service-1
  docker logs jenkins-kafka-notification-service-1
  ```
- Enable debug logging in `application.yml`:
  ```yaml
  logging:
    level:
      org.springframework: DEBUG
      org.springframework.kafka: DEBUG
      com.example: DEBUG
      org.apache.kafka: DEBUG
  ```
- Rebuild and restart:
  ```bash
  docker-compose up -d --build
  ```

### 7. Resource Issues
**Symptoms**:
- Services crash or are slow.

**Resolution**:
- Check memory and disk:
  ```bash
  free -m
  df -h
  ```
- Ensure >2GB free memory.

### 8. Port Conflicts
**Symptoms**:
- `Connection refused` errors.

**Resolution**:
- Check ports:
  ```bash
  netstat -tuln | grep 9092
  netstat -tuln | grep 2181
  ```
- Stop conflicting services or change ports in `docker-compose.yml`.

## Notes
- **Custom Images**: Preferred over `confluentinc/cp-kafka:7.3.0` due to missing CLI tools in Confluent images.
- **Serialization**: `JsonSerializer` resolved `SerializationException`. Validate `productId` to prevent invalid data.
- **Database**: Not used in this setup. Add configurations if needed.

## References
- Apache Kafka: https://kafka.apache.org/
- Confluent Platform: https://docs.confluent.io/
- Spring Kafka: https://spring.io/projects/spring-kafka


## how check order_topics created or not?

```copy

 kafka-topics.sh --bootstrap-server localhost:9092 --list | grep order_topics
   
```