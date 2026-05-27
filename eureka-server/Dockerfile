FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]