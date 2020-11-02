plugins {
    java
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("au.com.dius.pact") version "4.1.7"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation("junit:junit:4.12")
    testImplementation("au.com.dius.pact.provider:junit:4.1.7")
    testImplementation("au.com.dius.pact.consumer:junit:4.1.7")
}

tasks {
    named("test") {
        outputs.files("$buildDir/pacts")
    }
    register<Copy>("pactUpdate") {
        from("$buildDir/pacts")
        into("$projectDir/pacts")
        dependsOn("test")
    }
    matching { it.name.startsWith("pactVerify") }.configureEach {
        dependsOn("pactUpdate")
    }
}

pact {
    serviceProviders {
        create("nbp") {
            // All the provider properties are optional, and have sensible defaults (shown below)
            protocol = "http"
            host = "api.nbp.pl"
            port = 80
            // Again, you can define as many consumers for each provider as you need, but each must have a unique name
            hasPactWith("demo") {
                // currently supports a file path using file() or a URL using url()
                pactSource = file("pacts/demo-nbp.json")
            }
        }
    }
}