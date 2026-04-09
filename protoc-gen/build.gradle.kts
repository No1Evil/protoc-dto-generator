import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("org.graalvm.buildtools.native") version "1.0.0"
    `kotlin-dsl`
}

val projectVersion: String by project

group = "io.github.no1evil"
version = projectVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.28.0")
    implementation("com.google.protobuf:protobuf-java:4.34.1")
    implementation("com.google.protobuf:protobuf-java-util:4.34.1")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

tasks.jar {
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "io.github.no1evil.protogen.ProtoGen"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

graalvmNative {
    metadataRepository {
        enabled.set(false)
    }

    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
            mainClass.set("io.github.no1evil.protogen.ProtoGen")
            sharedLibrary.set(false)

            verbose.set(true)
            buildArgs.add("--no-fallback")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("--initialize-at-build-time=com.google.protobuf")
            buildArgs.add("--initialize-at-run-time=com.github.javaparser")
        }
    }
}