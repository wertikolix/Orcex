pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Orcex"

include(":orcex-core")
include(":orcex-layout")
include(":orcex-render-android")
include(":orcex-font-stix2-android")
include(":orcex-render-skia")
include(":orcex-render-compose")
