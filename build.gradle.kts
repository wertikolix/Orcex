plugins {
    kotlin("multiplatform") version "2.3.21" apply false
    id("com.android.library") version "9.2.1" apply false
    id("com.android.kotlin.multiplatform.library") version "9.2.1" apply false
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

subprojects {
    group = rootProject.group
    version = rootProject.version
}

val centralBundleDirectory = layout.buildDirectory.dir("central-bundle")
val cleanCentralBundle by tasks.registering(Delete::class) {
    delete(centralBundleDirectory)
}

val centralPublishTasks = subprojects.map { project ->
    project.tasks.matching { task -> task.name.endsWith("PublicationToCentralBundleRepository") }
}

tasks.register<Zip>("centralBundleZip") {
    dependsOn(cleanCentralBundle)
    dependsOn(centralPublishTasks)
    centralPublishTasks.forEach { tasks ->
        tasks.configureEach { mustRunAfter(cleanCentralBundle) }
    }
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set("orcex-${project.version}-central-bundle.zip")
    from(centralBundleDirectory)
}
