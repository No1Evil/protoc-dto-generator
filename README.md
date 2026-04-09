# Protoc DTO generator

## Description
Is a protoc extension to reduce boilerplate code writing DTO wrappers, enums 
and service interfaces for protobuf classes,
supporting auto-generated mappers without using any
other library *(such as MapStruct)*.

## Example of generated classes and .proto files

[Examples](docs/EXAMPLE.md)

## !! Note

The project was built to suit my needs and I won't maintain that project,
But it is written for using .proto docs as if you followed best-practises of
writing .proto files, maybe :D.

## Usage

```kotlin
repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/no1evil/protoc-dto-generator")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

```kotlin
protobuf.plugins {
    id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:4.34.1" }
    id("dto") { artifact = "io.github.no1evil:protoc-gen-dto:1.0" }
}

protobuf.generateProtoTasks {
    all().forEach { task ->
        task.plugins {
            id("grpc")
            id("dto")
        }
    }
}
```

## License
The project was built to suit my needs and I won't maintain that project,
so you are free to fork the project and make it for your needs.

[License](LICENSE)

