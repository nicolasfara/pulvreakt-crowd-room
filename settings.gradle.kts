import java.util.*

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.13.3"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "ACSOS-2023-pulverization-crowd-room".lowercase(Locale.getDefault())
