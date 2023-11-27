plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "cn.j1angvei"
version = "0.4"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("commons-io:commons-io:2.13.0")
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("com.google.code.gson:gson:2.8.9")
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "cn.j1angvei.logandecryption.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}


