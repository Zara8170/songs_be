##########
# Build stage
##########
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle wrapper and build files
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Source code
COPY src ./src

# Build Spring Boot fat jar (skip tests for faster image build)
RUN ./gradlew bootJar -x test

##########
# Runtime stage
##########
FROM eclipse-temurin:17-jre
WORKDIR /app

# Optional timezone
ENV TZ=Asia/Seoul

# Default profile can be overridden by environment
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_ADDRESS=0.0.0.0

# Copy built jar
COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
