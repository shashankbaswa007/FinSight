# ══════════════════════════════════════════════════════════════
# FinSight Backend – Multi-stage Docker Build
# Java 25 LTS (supports Sep 2034)
# ══════════════════════════════════════════════════════════════

# ── Stage 1: Build ──
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Run ──
FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="finsight-team"
LABEL description="FinSight – Personal Finance Analytics Platform"
LABEL java_version="25-LTS"

WORKDIR /app

RUN addgroup -S finsight && adduser -S finsight -G finsight
USER finsight

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
