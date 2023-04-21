import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.danilopianini.gradle.mavencentral.JavadocJar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.gradle.internal.os.OperatingSystem

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.npm.publish)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinx.serialization)
}

group = "it.nicolasfarabegoli.pulverization.crowdroom"

repositories {
    google()
    mavenCentral()
}

fun ShadowJar.jarConfiguration(jarName: String, mainClass: String) {
    archiveClassifier.set("all")
    archiveBaseName.set(jarName)
    archiveVersion.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    manifest {
        attributes("Main-Class" to mainClass)
    }
    val main by kotlin.jvm().compilations
    from(main.output)
    configurations += main.compileDependencyFiles as Configuration
    configurations += main.runtimeDependencyFiles as Configuration
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        val wearableJar by tasks.registering(ShadowJar::class) {
            description = "Generate jar for wearable device"
            group = "publishing"
            jarConfiguration("wearable", "it.nicolasfarabegoli.pulverization.crowd.wearable.WearableMainKt")
        }
        val laboratoryJar by tasks.registering(ShadowJar::class) {
            description = "Generate jar for laboratory device"
            group = "publishing"
            jarConfiguration("laboratory", "it.nicolasfarabegoli.pulverization.crowd.laboratory.LaboratoryMainKt")
        }
        val generateJars by tasks.registering {
            description = "Generate all the jars"
            group = "publishing"
            dependsOn(wearableJar, laboratoryJar)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.pulverization)
                implementation(libs.kotlinx.coroutine)
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.testing.common)
                implementation(libs.bundles.kotest.common)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.rabbitmq.reactor)
                implementation(libs.kotlinx.reactor)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
    }

    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    val nativeSetup: KotlinNativeTarget.() -> Unit = {
        compilations["main"].defaultSourceSet.dependsOn(kotlin.sourceSets["nativeMain"])
        compilations["test"].defaultSourceSet.dependsOn(kotlin.sourceSets["nativeTest"])
        binaries {
            sharedLib()
            staticLib()
        }
    }

    linuxX64(nativeSetup)
    // linuxArm64(nativeSetup)

    mingwX64(nativeSetup)

    macosX64(nativeSetup)
    macosArm64(nativeSetup)
    ios(nativeSetup)
    watchos(nativeSetup)
    tvos(nativeSetup)

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }

    val os = OperatingSystem.current()
    val excludeTargets = when {
        os.isLinux -> kotlin.targets.filterNot { "linux" in it.name }
        os.isWindows -> kotlin.targets.filterNot { "mingw" in it.name }
        os.isMacOsX -> kotlin.targets.filter { "linux" in it.name || "mingw" in it.name }
        else -> emptyList()
    }.mapNotNull { it as? KotlinNativeTarget }

    configure(excludeTargets) {
        compilations.configureEach {
            cinterops.configureEach { tasks[interopProcessingTaskName].enabled = false }
            compileTaskProvider.get().enabled = false
            tasks[processResourcesTaskName].enabled = false
        }
        binaries.configureEach { linkTask.enabled = false }

        mavenPublication {
            tasks.withType<AbstractPublishToMaven>().configureEach {
                onlyIf { publication != this@mavenPublication }
            }
            tasks.withType<GenerateModuleMetadata>().configureEach {
                onlyIf { publication.get() != this@mavenPublication }
            }
        }
    }
}

tasks.dokkaJavadoc {
    enabled = false
}

tasks.withType<JavadocJar>().configureEach {
    val dokka = tasks.dokkaHtml.get()
    dependsOn(dokka)
    from(dokka.outputDirectory)
}

signing {
    if (System.getenv("CI") == "true") {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

publishOnCentral {
    projectLongName.set("Template for Kotlin Multiplatform Project")
    projectDescription.set("A template repository for Kotlin Multiplatform projects")
    repository("https://maven.pkg.github.com/danysk/${rootProject.name}".toLowerCase()) {
        user.set("DanySK")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    developers {
                        developer {
                            name.set("Danilo Pianini")
                            email.set("danilo.pianini@gmail.com")
                            url.set("http://www.danilopianini.org/")
                        }
                    }
                }
            }
        }
    }
}

npmPublish {
    registries {
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
            val npmToken: String? by project
            authToken.set(npmToken)
            dry.set(npmToken.isNullOrBlank())
        }
    }
}

publishing {
    publications {
        publications.withType<MavenPublication>().configureEach {
            if ("OSSRH" !in name) {
                artifact(tasks.javadocJar)
            }
        }
    }
}
