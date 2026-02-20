# ──────────────────────────────────────────────────────────────
# Stage 1: Build
# ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first to cache dependency layer
COPY pom.xml ./
COPY .mvn/ ./.mvn/
COPY mvnw ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ ./src/
RUN ./mvnw package -DskipTests -q

# ──────────────────────────────────────────────────────────────
# Stage 2: Runtime (non-root, minimal image)
# ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S fitnessapp && adduser -S fitnessapp -G fitnessapp

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership
RUN chown fitnessapp:fitnessapp app.jar

USER fitnessapp

EXPOSE 8080

# Use exec form to propagate signals properly
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
