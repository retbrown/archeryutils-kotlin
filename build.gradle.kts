import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    `maven-publish`
}

group = "io.github.retbrown"
version = providers.gradleProperty("VERSION_NAME").get()

val githubRepo = "retbrown/archeryutils-kotlin"

kotlin {
    explicitApi()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ArcheryUtils"
            isStatic = true
        }
    }

    jvm()

    android {
        namespace = "io.github.retbrown.archeryutils"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dokka {
    // Fail the build on undocumented public API or broken KDoc links.
    dokkaPublications.html {
        failOnWarning.set(true)
    }
    dokkaSourceSets.configureEach {
        reportUndocumented.set(true)
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl("https://github.com/$githubRepo/blob/main/src")
            remoteLineSuffix.set("#L")
        }
    }
}

kover {
    reports {
        verify {
            rule {
                minBound(95)
            }
        }
    }
}

// Javadoc jars are required by Maven Central and nice to have elsewhere; Dokka provides the content.
val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        pom {
            name.set("archeryutils-kotlin")
            description.set(
                "Archery utilities for Kotlin Multiplatform: targets, rounds, handicaps and " +
                    "Archery GB classifications. A port of the Python library archeryutils.",
            )
            url.set("https://github.com/$githubRepo")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("retbrown")
                    name.set("Robert Brown")
                    url.set("https://github.com/retbrown")
                }
            }
            scm {
                url.set("https://github.com/$githubRepo")
                connection.set("scm:git:git://github.com/$githubRepo.git")
                developerConnection.set("scm:git:ssh://git@github.com/$githubRepo.git")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubRepo")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}
