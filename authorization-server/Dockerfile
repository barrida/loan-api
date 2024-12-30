# Stage 1: Build the application
FROM gradle:8.9.0-jdk17-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy the Gradle build files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Build the application
RUN gradle build

# Debug: List the contents of the build/libs directory
RUN ls -l /app/build/libs/

# Use Amazon Corretto 17 as the base image
FROM amazoncorretto:17-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the application's JAR file into the container
COPY --from=builder /app/build/libs/authorization-server.jar /app/authorization-server.jar

# Command to run the application
CMD ["java", "-jar", "/app/authorization-server.jar"]
