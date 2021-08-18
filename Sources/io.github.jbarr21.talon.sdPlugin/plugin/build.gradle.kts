import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.andrewoma.kommon:kommon:0.14")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("ch.qos.logback:logback-classic:1.0.9")
    implementation("ch.qos.logback:logback-core:1.0.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "io.github.jbarr21.talon.TalonToggleCli"
}
