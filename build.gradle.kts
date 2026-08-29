plugins {
    kotlin("jvm") version "2.4.0"
    application
}

application {
    mainClass.set("xyz.xenoo.sipai.MainKt")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}