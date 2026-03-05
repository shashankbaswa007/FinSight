FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="finsight-team"
LABEL description="FinSight – Personal Finance Analytics Platform"

WORKDIR /app

COPY target/finsight-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
