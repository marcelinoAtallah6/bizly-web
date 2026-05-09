FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/authorization-authentication-0.0.1-SNAPSHOT.jar authorization.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "authorization.jar"]