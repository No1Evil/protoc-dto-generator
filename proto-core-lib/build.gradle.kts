plugins {
    id("java")
    `maven-publish`
}

group = "io.github.no1evil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java-util:4.34.1")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // Имя артефакта, под которым он будет лежать в io.github.no1evil
            artifactId = "proto-core-lib"
        }
    }
    // Блок repositories здесь не обязателен для mavenLocal,
    // так как задача publishToMavenLocal встроена по умолчанию
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
}