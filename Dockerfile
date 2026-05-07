# =====================================================
# Dockerfile — Blogger Spring Boot Backend
# Java 11, Maven, Port 8081
# =====================================================

# Stage 1: Build the application
FROM eclipse-temurin:11-jdk-alpine AS build
WORKDIR /build

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable (important for Linux containers)
RUN chmod +x mvnw

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# =====================================================
# Stage 2: Run the application
# =====================================================
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Create directory for images (your app stores images here)
RUN mkdir -p /app/images

# Copy the JAR from build stage
COPY --from=build /build/target/blogger-apps-apis-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8081 (your server.port)
EXPOSE 8081
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
