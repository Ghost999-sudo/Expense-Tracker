plugins {
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    application
}

group = "com.example.expensetracker"
version = "1.0.0"

application {
    mainClass.set("com.example.expensetracker.server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.logback.classic)

    testImplementation(libs.junit)
}
