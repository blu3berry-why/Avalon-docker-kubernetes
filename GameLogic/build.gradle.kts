import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application

    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"

    //FatJar
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

//Heroku
/*
tasks.create("stage") {
    dependsOn("installDist")
}*/
//--

//Docker - fatjar
tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "io.ktor.server.netty.EngineMain"))
        }
    }
}
//--


group = "com.example"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    // kmongo
    implementation("org.litote.kmongo:kmongo:4.3.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.3.0")

    //serialisable
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

    //passwordHash
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}