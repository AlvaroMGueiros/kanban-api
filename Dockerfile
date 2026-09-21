FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/target/kanban-api-*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
