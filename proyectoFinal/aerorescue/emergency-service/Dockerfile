# ── Stage 1: Build ──────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy parent POM
COPY pom.xml ./pom.xml

# Copy ALL module POMs — required so Maven can resolve multi-module structure
COPY auth-service/pom.xml         ./auth-service/pom.xml
COPY api-gateway/pom.xml          ./api-gateway/pom.xml
COPY emergency-service/pom.xml    ./emergency-service/pom.xml
COPY drone-service/pom.xml        ./drone-service/pom.xml
COPY mission-service/pom.xml      ./mission-service/pom.xml
COPY alert-service/pom.xml        ./alert-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY audit-service/pom.xml        ./audit-service/pom.xml

# Download dependencies (cache layer — only re-runs if any pom.xml changes)
RUN mvn dependency:go-offline -pl emergency-service -am -q

# Copy source of this module only
COPY emergency-service/src ./emergency-service/src

# Build
RUN mvn clean package -pl emergency-service -am -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S aerorescue && adduser -S aerorescue -G aerorescue
USER aerorescue

COPY --from=builder /build/emergency-service/target/emergency-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
