plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "protoc-dto-generator"
include("protoc-gen")
include("proto-core-lib")