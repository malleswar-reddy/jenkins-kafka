# Purging All Unused or Dangling Images, Containers, Volumes, and Networks

### This command will remove all unused or dangling images, containers, volumes, and networks.
### It is a good practice to run this command periodically to free up disk space.
```copy
docker system prune
```

### This command will remove all unused or dangling images, containers, volumes, and networks.
### It is a good practice to run this command periodically to free up disk space.
```copy
docker system prune -a
```
### This command will remove all unused or dangling images, containers, volumes, and networks.
### It is a good practice to run this command periodically to free up disk space.
```copy
docker system prune -a --volumes
```
### This command will remove all unused or dangling images, containers, volumes, and networks.
### It is a good practice to run this command periodically to free up disk space.
```copy
docker system prune -a --volumes --force
```
### This command will remove all unused or dangling images, containers, volumes, and networks.

## error:  zsh: command not found: docker
### This error occurs when the Docker CLI is not installed or not in the system's PATH.
✅ Here's how to fix it:
🔧 1. Check if Docker CLI is actually installed
```copy
ls /Applications/Docker.app/Contents/Resources/bin/

```

🔧 2. Manually Add Docker to Your PATH

Add this to your copy config file (~/.zshrc for Zsh):
```copy
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
``` 

🔧 3. Restart your terminal or run the following command to apply the changes:
```copy
source ~/.zshrc
```
🔧 4. Verify that Docker is now in your PATH:
```copy
which docker
```

To do that, run:

echo 'export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

Then try:

docker --version

## host  docker-desktop
```copy
sudo nano  /private/etc/hosts 

::1             localhost
#192.168.0.149   kafka

```


# Next Steps:
## Test OrderService:


Use Postman or a similar tool to send a POST request to http://localhost:8081/orders with a sample order payload:
```json
{
"orderId": "ORD001",
"productId": "PROD001",
"quantity": 10
}
``` 
2.Verify Logs:


Check the logs of OrderService, StockService, and NotificationService to ensure the order is processed:
```copy
docker logs jenkins-kafka-order-service-1
docker logs jenkins-kafka-stock-service-1
docker logs jenkins-kafka-notification-service-1
```
 3.Inspect Topics:


Consume messages from order_topics to verify the order is published:
```copy
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics --from-beginning
```
 4.Check Dead-Letter Topic (if needed):
Consume messages from order_topics-dlt to verify any failed messages:
```copy
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order_topics-dlt --from-beginning
```

## First I try:
```bash
docker system prune -a


docker system prune --volumes


cd /var/lib
sudo rm -rf docker
systemctl restart docker## First I try:
```bash
docker system prune -adocker system prune --volumes