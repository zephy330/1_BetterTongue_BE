# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle wrapper and build files first (for caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build with limited memory for free tier
RUN ./gradlew bootJar --no-daemon -Dorg.gradle.jvmargs="-Xmx512m"

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run with prod profile and limited memory
ENTRYPOINT ["java", "-Xmx400m", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
