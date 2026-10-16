FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY target/userService-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]