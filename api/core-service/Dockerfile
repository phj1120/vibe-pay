FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY common-lib ./common-lib
RUN ./gradlew :common-lib:common-core:jar :common-lib:common-security:jar :common-lib:common-messaging:jar --no-daemon
COPY core-service ./core-service
RUN ./gradlew :core-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/core-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
