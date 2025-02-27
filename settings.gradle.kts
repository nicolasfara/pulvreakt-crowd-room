import java.util.*

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.19.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.20"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
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

rootProject.name = "pulvreakt-crowd-room"
