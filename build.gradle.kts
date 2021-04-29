import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
        maven { url = uri("https://jetbrains.bintray.com/intellij-third-party-dependencies") }
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.0")
    }
}

plugins {
    kotlin("jvm") version "1.4.32"
    id("org.jetbrains.compose") version "0.4.0-build184"
    id("org.jetbrains.kotlin.kapt") version "1.4.32"
    id("com.squareup.sqldelight") version "1.5.0"
}

group = "me.kohru"
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

sqldelight {
    database("SettingsDB") { // This will be the name of the generated database class.
        packageName = "settings.database"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    implementation("io.insert-koin:koin-core:3.0.1")

    implementation("com.squareup.sqldelight:sqlite-driver:1.5.0")
    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.5.0")

    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.5")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
    kotlinOptions.freeCompilerArgs += "-Xinline-classes"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SRC_Client"
            packageVersion = "1.0.0"
        }
    }
}