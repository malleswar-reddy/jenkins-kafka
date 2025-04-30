FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
COPY src/main/resources /app/config
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]