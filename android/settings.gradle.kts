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

rootProject.name = "CheckInSOS"

// Clean Architecture modules: :domain (pure Kotlin), :data (mock store), :app (Presentation, Compose).
include(":domain", ":data", ":app")
