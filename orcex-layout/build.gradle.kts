plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
    id("signing")
}

kotlin {
    android {
        namespace = "ru.wertik.orcex.layout"
        compileSdk = 36
        minSdk = 21
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
        withHostTest {}
    }
    jvm()
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    wasmJs {
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":orcex-core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

apply(from = rootProject.file("gradle/publish-module.gradle.kts"))
