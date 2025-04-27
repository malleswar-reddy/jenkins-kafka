# jenkins-kafka
jenkins-kafka

## docker compose up
```bash
 docker-compose up -d --build 
 or
 docker compose up -d --build

```
## docker compose down
```bash
 docker-compose down 
```

## Summary docker Commands

```bash
docker stop $(docker ps -q)
docker rm $(docker ps -aq)
docker network prune
docker volume prune


```

## docker tips
```bash
docker ps -a          # all containers
docker network ls     # all networks
docker volume ls      # all volumes
```

| Problem                           | Solution                                                          |
|-----------------------------------|-------------------------------------------------------------------|
| Missing docker-credential-desktop | Reinstall Docker Desktop                                          |
|                                   | Remove \`"credsStore": "desktop"\` from \`~/.docker/config.json\` |

