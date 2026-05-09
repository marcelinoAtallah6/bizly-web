FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/apigateway-0.0.1-SNAPSHOT.jar apigateway.jar
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "apigateway.jar"]