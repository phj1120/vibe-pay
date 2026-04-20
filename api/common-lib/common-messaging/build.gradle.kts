plugins {
    `java-library`
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

dependencyManagement {
    imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7") }
}

dependencies {
    api(project(":common-lib:common-core"))
    api("org.springframework.kafka:spring-kafka")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions { freeCompilerArgs += "-Xjsr305=strict"; jvmTarget = "21" }
}
