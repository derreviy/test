plugins {
    kotlin("jvm") version "2.0.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.2.0")
}

