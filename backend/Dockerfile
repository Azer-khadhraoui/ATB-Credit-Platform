# --- Build stage -------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source for faster rebuilds.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Run as a non-root user (DevSecOps: no container process runs as root).
RUN addgroup -S atb && adduser -S atb -G atb
RUN mkdir -p /app/uploads && chown -R atb:atb /app

COPY --from=build /app/target/*.jar app.jar

USER atb
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
