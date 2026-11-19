import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Presentation layer (MVVM, Jetpack Compose). AGP 9 built-in Kotlin + the Compose compiler plugin.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
    jacoco
}

android {
    namespace = "uk.checkinsos.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "uk.checkinsos.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // JaCoCo coverage for the emulator UI tests (uiCoverageVerify below).
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    testCoverage { jacocoVersion = libs.versions.jacoco.get() }

    // Robots + end-to-end flows run on the JVM (Robolectric, counts for Kover) and on the emulator.
    sourceSets {
        getByName("test").kotlin.directories.add("src/sharedTest/kotlin")
        getByName("androidTest").kotlin.directories.add("src/sharedTest/kotlin")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

jacoco { toolVersion = libs.versions.jacoco.get() }

dependencies {
    implementation(project(":data"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.compose.ui.test.junit4.accessibility)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    // Accessibility Test Framework checks on every action (enableAccessibilityChecks()).
    androidTestImplementation(libs.compose.ui.test.junit4.accessibility)
}

// --- UI-test coverage gate: emulator tests alone must cover >= 80% of the presentation layer. ---
// Scope: feature.**, designsystem.**, navigation.** (see CLAUDE.md). Exclusions mirror Kover's.
val uiCoverageClasses = fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
    include("uk/checkinsos/app/feature/**", "uk/checkinsos/app/designsystem/**", "uk/checkinsos/app/navigation/**")
    exclude("**/*ComposableSingletons*", "**/*\$inlined*", "**/*Preview*")
}
val uiCoverageData = fileTree(layout.buildDirectory.dir("outputs/code_coverage/debugAndroidTest/connected")) {
    include("**/*.ec")
}

val uiCoverageReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "HTML/XML report of presentation-layer coverage from the emulator UI tests."
    mustRunAfter("createDebugAndroidTestCoverageReport")
    executionData.setFrom(uiCoverageData)
    classDirectories.setFrom(uiCoverageClasses)
    sourceDirectories.setFrom("src/main/java")
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("uiCoverageVerify") {
    group = "verification"
    description = "Fails when the emulator UI tests cover < 80% of presentation-layer lines."
    dependsOn(uiCoverageReport)
    executionData.setFrom(uiCoverageData)
    classDirectories.setFrom(uiCoverageClasses)
    sourceDirectories.setFrom("src/main/java")
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

// Kover's total variant skips Android build variants; the "all" variant adds the debug unit tests
// (ViewModel tests, Robolectric screen tests and the shared flows) to the root 80% gate.
kover { currentProject { createVariant("all") { add("debug") } } }
