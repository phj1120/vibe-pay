FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY common-lib ./common-lib
COPY claim-service ./claim-service

RUN ./gradlew :common-lib:common-core:jar :common-lib:common-security:jar :common-lib:common-messaging:jar --no-daemon -q && \
    ./gradlew :claim-service:bootJar --no-daemon -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/claim-service/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
