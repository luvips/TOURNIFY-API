plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.gradleup.shadow") version "9.2.2"
}

group = "com"
version = "0.0.1"
val ktor_version= "3.3.2"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // 2. INYECCIÓN DE DEPENDENCIAS (Koin para Ktor)
    implementation("io.insert-koin:koin-ktor:3.5.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")

    // --- AWS S3 (SDK de Kotlin) ---
    implementation("aws.sdk.kotlin:s3:1.0.13")
    implementation("aws.smithy.kotlin:aws-signing-default:1.0.13")

    // --- Ktor Client (Requerido por AWS SDK para hacer peticiones HTTP) ---
    implementation("io.ktor:ktor-client-cio:3.0.0")

    implementation("org.mindrot:jbcrypt:0.4")
}

tasks {
    shadowJar {

        archiveBaseName.set("tournify")
        archiveClassifier.set("all")
        archiveVersion.set("")

        isZip64 = true

        mergeServiceFiles()
    }
}

kotlin {
    jvmToolchain(21)
}