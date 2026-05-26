plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
    id("signing")
}

kotlin {
    jvm()
    linuxX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":orcex-core"))
            api(project(":orcex-layout"))
            api("org.jetbrains.skiko:skiko:0.148.1")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.148.1")
        }
    }
}

apply(from = rootProject.file("gradle/publish-module.gradle.kts"))
