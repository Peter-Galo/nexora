# Multi-stage build for Angular + Spring Boot application

# Stage 1: Build Angular frontend
FROM node:20-alpine AS frontend-build

WORKDIR /app/client

# Copy package files
COPY client/package*.json ./

# Install dependencies (including dev dependencies for build)
RUN npm ci

# Copy source code (excluding node_modules via .dockerignore)
COPY client/ ./

# Build Angular app for production
RUN npm run build

# Stage 2: Build Spring Boot backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-build

WORKDIR /app

# Copy Maven files
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Download dependencies (for better caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Copy built frontend assets to Spring Boot static resources
COPY --from=frontend-build /app/client/dist/client ./src/main/resources/static

# Build the application
RUN mvn clean package -DskipTests

# Stage 3: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -g 1001 -S nexora && \
    adduser -S nexora -u 1001 -G nexora

WORKDIR /app

# Copy the built JAR file
COPY --from=backend-build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown nexora:nexora app.jar

# Switch to non-root user
USER nexora

# Expose port
EXPOSE 8080

# Health check - using basic endpoint since actuator is not included
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
