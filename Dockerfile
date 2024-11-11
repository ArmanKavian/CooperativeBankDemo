FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY target/CooperativeBankDemo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Monitoring
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]