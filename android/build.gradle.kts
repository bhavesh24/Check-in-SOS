// Root project: declares plugin versions and aggregates Kover coverage for the 80% overall gate.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":domain"))
    kover(project(":data"))
    kover(project(":app"))
}

kover {
    // Merged JVM (:domain, :data) + Android debug (:app) coverage. `koverVerify` / `koverHtmlReport`
    // below delegate to this variant so the documented commands measure every layer.
    currentProject { createVariant("all") {} }
    reports {
        filters {
            excludes {
                // Composition root / entry point / previews (see CLAUDE.md "Coverage gates").
                // Keep in sync with app/build.gradle.kts (uiCoverageVerify) and the README.
                classes(
                    "uk.checkinsos.app.MainActivity*",
                    "uk.checkinsos.app.AppContainer*",
                    "uk.checkinsos.app.previews.*",
                    "*ComposableSingletons*",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }
        verify {
            rule("Overall line coverage") {
                minBound(80)
            }
        }
    }
}

tasks.named("koverVerify") { dependsOn("koverVerifyAll") }
tasks.named("koverHtmlReport") { dependsOn("koverHtmlReportAll") }
tasks.named("koverLog") { dependsOn("koverLogAll") }
