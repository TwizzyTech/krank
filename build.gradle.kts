plugins {
    kotlin("jvm") version "2.1.10"
}

group = "twizzy.tech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Minestom Library
    implementation("net.minestom:minestom-snapshots:1_21_4-bb14804d42")

    // SLF4J Logging
    implementation("org.slf4j:slf4j-simple:2.0.14")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}