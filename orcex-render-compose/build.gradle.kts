plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
    id("signing")
}

kotlin {
    android {
        namespace = "ru.wertik.orcex.render.compose"
        compileSdk = 36
        minSdk = 21
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
        withHostTest {}
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    wasmJs {
        browser {
            testTask {
                // Browser-based wasm tests require a managed browser; klib compilation
                // is verified in CI instead.
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":orcex-layout"))
            api("org.jetbrains.compose.ui:ui:1.11.0")
            api("org.jetbrains.compose.foundation:foundation:1.11.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

apply(from = rootProject.file("gradle/publish-module.gradle.kts"))
