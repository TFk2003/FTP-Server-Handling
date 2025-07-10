# Stage 1: Build with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Use system-installed Maven to build the JAR (skip tests for faster build)
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8081

# Set environment file if needed (optional)
# ENV SPRING_CONFIG_LOCATION=classpath:/application.properties

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
