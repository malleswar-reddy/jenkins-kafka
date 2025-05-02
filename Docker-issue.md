# README: Dockerized Microservices with Kafka

This project sets up a microservices architecture using Docker, Kafka, and related services (OrderService, StockService, NotificationService). This README provides instructions for managing Docker resources, troubleshooting common issues, and testing the services.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Docker Cleanup](#docker-cleanup)
3. [Troubleshooting Docker CLI](#troubleshooting-docker-cli)
4. [Rebuilding and Running Notification Service](#rebuilding-and-running-notification-service)
5. [Testing OrderService](#testing-orderservice)
6. [Inspecting Kafka Topics](#inspecting-kafka-topics)
7. [Additional Notes](#additional-notes)

## Prerequisites
- **Docker** and **Docker Compose** installed.
- **Kafka** running in a Docker container (named `kafka`).
- **Postman** or a similar tool for API testing.
- Access to `/private/etc/hosts` for local host configuration.

## Docker Cleanup
To free up disk space, periodically remove unused Docker resources (images, containers, volumes, networks).

### Commands
1. Remove unused resources:
   ```bash
   docker system prune
   ```
2. Remove all unused resources, including images not used by any container:
   ```bash
   docker system prune -a
   ```
3. Remove unused volumes as well:
   ```bash
   docker system prune -a --volumes
   ```
4. Force removal without confirmation:
   ```bash
   docker system prune -a --volumes --force
   ```

### Advanced Cleanup (if needed)
To completely reset Docker (use with caution):
```bash
cd /var/lib
sudo rm -rf docker
systemctl restart docker
```

## Troubleshooting Docker CLI
If you encounter the error `zsh: command not found: docker`, the Docker CLI is either not installed or not in your system's PATH.

### Steps to Fix
1. **Check if Docker is installed**:
   ```bash
   ls /Applications/Docker.app/Contents/Resources/bin/
   ```
   Ensure `docker` is listed.

2. **Add Docker to PATH**:
   Add the following to your `~/.zshrc`:
   ```bash
   export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
   ```
   Or run:
   ```bash
   echo 'export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"' >> ~/.zshrc
   ```

3. **Apply changes**:
   ```bash
   source ~/.zshrc
   ```

4. **Verify Docker**:
   ```bash
   which docker
   docker --version
   ```

### Configure Hosts (if needed)
If services like Kafka require host mapping:
```bash
sudo nano /private/etc/hosts
```
Add:
```
::1             localhost
#192.168.0.149   kafka
```

## Rebuilding and Running Notification Service
If the Notification Service fails, rebuild and redeploy it.

### Steps
1. **Fix the Dockerfile** (ensure it’s correct; not provided here).
2. **Rebuild the image**:
   ```bash
   docker build -t notification-service .
   ```
3. **Run the container**:
   ```bash
   docker run -d --name notification-service -p 8083:8083 notification-service
   ```
4. **Check logs**:
   ```bash
   docker logs notification-service
   ```

## Testing OrderService
Test the OrderService by sending a sample order and verifying the process.

### Steps
1. **Send a POST request**:
   Use Postman to send a POST request to `http://localhost:8081/orders`:
   ```json
   {
       "orderId": "ORD001",
       "productId": "PROD001",
       "quantity": 10
   }
   ```

2. **Verify logs**:
   Check logs for each service:
   ```bash
   docker logs jenkins-kafka-order-service-1
   docker logs jenkins-kafka-stock-service-1
   docker logs jenkins-kafka-notification-service-1
   ```

## Inspecting Kafka Topics
Verify that messages are published to Kafka topics.

### Steps
1. **Consume messages from `order_topics`**:
   ```bash
   docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics --from-beginning
   ```

2. **Check dead-letter topic (`order_topics-dlt`)**:
   ```bash
   docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics-dlt --from-beginning
   ```

## Additional Notes
- Ensure Kafka is accessible at `kafka:9092` within the Docker network.
- If services fail, check Docker Compose configurations and network settings.
- Regularly clean up Docker resources to avoid disk space issues.