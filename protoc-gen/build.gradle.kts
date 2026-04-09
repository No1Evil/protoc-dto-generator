import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("org.graalvm.buildtools.native") version "1.0.0"
    `maven-publish`
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
            buildArgs.add("--initialize-at-build-time=com.google.protobuf,com.github.javaparser")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("native") {
            groupId = "io.github.no1evil"
            artifactId = "protoc-gen-dto"
            version = projectVersion

            val exeFile = file("build/native/nativeCompile/protoc-gen.exe")

            artifact(exeFile) {
                classifier = "windows-x86_64"
                extension = "exe"
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/no1evil/protoc-dto-generator")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}