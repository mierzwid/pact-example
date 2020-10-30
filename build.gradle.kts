plugins {
    java
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("au.com.dius.pact") version "4.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

pact {
    serviceProviders {
        create("nbp") {
            // All the provider properties are optional, and have sensible defaults (shown below)
            protocol = "http"
            host = "localhost"
            port = 8080
            path = "/"
            // Again, you can define as many consumers for each provider as you need, but each must have a unique name
            hasPactWith("demo") {
                // currently supports a file path using file() or a URL using url()
                pactSource = file("pact/nbp-demo-pact.json")
            }
        }
    }
}