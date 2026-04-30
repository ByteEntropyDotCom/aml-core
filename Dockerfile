# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy only the pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S amlgroup && adduser -S amluser -G amlgroup
USER amluser

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Configure JVM for container environments and Virtual Threads
ENTRYPOINT ["java", \
            "-XX:+UseParallelGC", \
            "-Dspring.threads.virtual.enabled=true", \
            "-jar", "app.jar"]
