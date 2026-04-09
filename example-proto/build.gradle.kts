import com.google.protobuf.gradle.id

plugins {
    id("java")
    `java-library`
    id("com.google.protobuf") version "0.9.6"
}

group = "io.github.no1evil.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Enabling base mappers
    implementation(project(":proto-core-lib"))

    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:4.28.2")

    implementation("io.grpc:grpc-stub:1.66.0")
    implementation("io.grpc:grpc-protobuf:1.66.0")
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.66.0"
        }
        // If you want to generate everything in one sourceSet
//        id("all"){
//            path = project.file("script/proto-gen.bat").absolutePath
//        }
        id("dto") {
            path = project.file("script/proto-gen.bat").absolutePath
        }
        id("mappers") {
            path = project.file("script/proto-gen.bat").absolutePath
        }
        id("service") {
            path = project.file("script/proto-gen.bat").absolutePath
        }

    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")

                // If you want to generate everything in one sourceSet
                // id("all")

                // If you want to separate the sourceSet
                id("dto"){
                    option("target=dto")
                }
                id("mappers"){
                    option("target=mapper")
                }
            }
        }
    }
}

sourceSets {
    main {

        proto {
            srcDir("proto")
        }

        java {
            srcDirs("src/main/java")
        }

    }
}