plugins {
    kotlin("jvm") version "2.1.10"
}

group = "twizzy.tech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://reposilite.worldseed.online/public")
    maven("https://repo.opencollab.dev/main/")

}

dependencies {
    // Minestom Library
    implementation("net.minestom:minestom-snapshots:1_21_4-bb14804d42")

    // SLF4J Logging
    implementation("org.slf4j:slf4j-simple:2.0.14")

    // MongoDB Driver
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.10.2")

    // Redis Client Driver
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")

    // Command Framework
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.9")
    implementation("io.github.revxrsal:lamp.minestom:4.0.0-rc.9")

    // Floodgate
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")

    // KotStom
    implementation("net.bladehunt:kotstom:0.4.0-beta.0")

    // Minestom Coroutine
    implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-api:2.21.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-core:2.21.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
