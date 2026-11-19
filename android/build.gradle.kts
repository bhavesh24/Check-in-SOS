// Root project: declares plugin versions and aggregates Kover coverage for the 80% overall gate.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":domain"))
    kover(project(":data"))
    // Phase 3: kover(project(":app"))
}

kover {
    reports {
        filters {
            excludes {
                // Composition root / entry point / previews (see CLAUDE.md "Coverage gates").
                classes("uk.checkinsos.app.MainActivity*", "uk.checkinsos.app.AppContainer*")
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
