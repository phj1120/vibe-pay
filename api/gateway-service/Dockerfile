FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY common-lib ./common-lib
RUN ./gradlew :common-lib:common-core:jar :common-lib:common-security:jar --no-daemon
COPY gateway-service ./gateway-service
RUN ./gradlew :gateway-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/gateway-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
