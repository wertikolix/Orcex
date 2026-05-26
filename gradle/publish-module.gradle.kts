import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

plugins.apply("maven-publish")
plugins.apply("signing")

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "CentralBundle"
            url = rootProject.layout.buildDirectory.dir("central-bundle").get().asFile.toURI()
        }
    }
    publications.withType(MavenPublication::class.java).configureEach {
        val publicationName = name
        val documentationJar = tasks.register<Jar>("${publicationName}DocumentationJar") {
            archiveBaseName.set("${project.name}-${publicationName}")
            archiveClassifier.set("javadoc")
            from(rootProject.file("README.md"))
        }
        artifact(documentationJar)
        pom {
            name.set("Orcex ${project.name.removePrefix("orcex-")}")
            description.set("Ultra-lightweight native LaTeX math library module ${project.name} for Kotlin Multiplatform.")
            url.set(providers.gradleProperty("POM_URL"))
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
                if (project.name == "orcex-font-stix2-android") {
                    license {
                        name.set("SIL Open Font License, Version 1.1")
                        url.set("https://openfontlicense.org/open-font-license-official-text/")
                        distribution.set("repo")
                    }
                }
            }
            developers {
                developer {
                    id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                    name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                }
            }
            scm {
                url.set(providers.gradleProperty("POM_SCM_URL"))
                connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
                developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
            }
        }
    }
}

extensions.configure<SigningExtension> {
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(extensions.getByType<PublishingExtension>().publications)
    }
    isRequired = signingKey != null && !project.version.toString().endsWith("-SNAPSHOT")
}

tasks.withType(org.gradle.api.publish.maven.tasks.PublishToMavenRepository::class.java).configureEach {
    doFirst {
        if (!project.version.toString().endsWith("-SNAPSHOT") && providers.gradleProperty("signingKey").orNull == null) {
            error("A release publication requires -PsigningKey and -PsigningPassword.")
        }
    }
}
