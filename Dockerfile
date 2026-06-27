# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies to cache this layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package (skipping tests for faster deployment)
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Document that the service listens on port 8080 (which will map to the PORT env var dynamically)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
