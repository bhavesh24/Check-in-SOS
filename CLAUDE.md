# Check-in SOS — working notes for Claude

Two native apps built from one design: an iOS app (SwiftUI) and an Android app (Jetpack Compose).
This build covers only the non-login screens. There is no backend, auth or real notifications; everything runs on mock data.
This file holds what you need day to day.

## Sources of truth
- **`docs/screens.md`** holds every screen's copy, flow, step counts, test IDs, design tokens, mock data and domain rules.
  - If behaviour must change, edit `docs/screens.md` first, then update **both** apps the same way.
- Both apps must show identical copy, mock data, test IDs and domain behaviour, and each side's tests mirror the other's.

## Architecture: Clean Architecture + MVVM (same layout in both apps)
| Layer | iOS | Android | May depend on |
|---|---|---|---|
| Domain | `ios/Packages/CheckInCore/Sources/Domain` | `android/domain` (pure Kotlin/JVM) | nothing (Foundation / `java.time` only) |
| Data | `ios/Packages/CheckInCore/Sources/Data` | `android/data` | Domain |
| Presentation | `ios/CheckInSOS` app target (phase 2) | `android/app` (phase 3) | Domain; Data **only** in `AppContainer` |

Rules:
- Domain never imports UI frameworks or the Data layer. Repositories are protocols/interfaces in Domain, and Data implements them.
- **Use cases do one action each** (`SubmitAssessment`, `GetLovedOnes`, `GetLovedOneStatus`, `UpdateNeighbour`, `GetTimeline`, `GetNeighbours`, `GetPatients`, `SimulateAlert`, `AcknowledgeAlert`, `MarkArrived`, `EscalateAlert`, `RecordCareOutcome`).
  - Swift calls them with `callAsFunction`; Kotlin uses `operator fun invoke`.
  - Repositories and use cases are `async throws` on iOS and `suspend` on Android.
- **Domain services:**
  - `ScoreCalculator`: a **placeholder** formula (Good+Pleasant = 76, Ill+Anxious = 38; ≥70 is good).
  - `EscalationPolicy`: 50–69 → 30 min, 30–49 → 20 min, 0–29 → 15 min, missed check-in → 15 min (placeholder).
  - `NeighbourValidator`
  - `TimeProvider`: an injectable clock. On iOS it isn't called `Clock`, which would clash with Swift's protocol.
- **Errors:** `DomainError` (Swift) and `DomainException` (Kotlin), each with `notFound`, `noAlertSubject` and `invalidNeighbour(NeighbourValidationError)`.
- **Data:** `MockDataSource` is an in-memory store (a Swift `actor`; a Kotlin class guarded by a `Mutex`) seeded from `MockFixtures`.
  - `MockFixtures.fixedNow` / `FIXED_NOW` = 2026-01-15T09:00:00Z.
  - The current older adult is `oa-1`.
- **Presentation (phases 2/3):**
  - One View + ViewModel per screen.
  - On iOS, ViewModels are `@MainActor @Observable` and Views hold them in `@State var viewModel`.
  - On Android, ViewModels are `androidx.lifecycle.ViewModel` subclasses exposing `StateFlow<UiState>`, collected with `collectAsStateWithLifecycle()`.
  - Views only render state and forward intents.
  - ViewModels never touch navigation. They report outcomes through closures (`onSubmitted`, `onAlert`, …), and the router maps those onto the path.
    - iOS: `AppRouter` (`[Route]` path) + `RootView` in `App/AppRouter.swift`; `AppContainer.make…` factories build every ViewModel.
  - Shared presentation types live in `Features/Shared` (`AlertSummary`, `FlowSteps`, `DisplayText`). The neighbour and hospital alerts share one `HelpAlert` screen, and the five hospital questions are one table-driven `CareStep` screen.
  - iOS: `#Preview`s go only in `CheckInSOS/Previews/` (excluded from coverage). Back is a custom toolbar button (`withBackButton()`, ID `nav.back`), and the edge-swipe gesture is kept.
  - Android:
    - Navigation Compose with type-safe `@Serializable` routes (`navigation/Routes.kt`). Alert routes carry the `AlertSummary` as flat primitives.
    - `AppNavHost` maps events to the back stack. ViewModels extend `FlowViewModel<UiState, Event>` (a `StateFlow` plus a one-off event `Channel`), are built with `viewModel { container.makeX() }` and never hold navigation lambdas.
    - Each screen is `XScreen(viewModel, nav callbacks)`, which collects state and events, plus a stateless `XContent(state, intents)` that the Robolectric screen tests render directly.
    - `@Preview`s go only in `uk.checkinsos.app.previews` (excluded). The top-app-bar title has the Android-only tag `screen.title`.
- **DI:** a manual composition root (`AppContainer`) wires everything through constructor injection; there's no DI framework.
  - Test mode uses fresh mock data and `FixedTimeProvider(fixedNow)`. On iOS that's the `-ui-testing` launch argument; on Android it's a test `AppContainer`.
- **UI conventions:** every interactive element gets the test ID listed in `docs/screens.md`, via `accessibilityIdentifier` on iOS and `Modifier.testTag` on Android.
  - UI tests use robot (page-object) helpers.
  - The 999/111 buttons must **never** dial; they show "Demo: would call 999" instead.
  - iOS XCUITest gotchas, all handled in `CheckInSOSUITests/Robots/Robot.swift`:
    - Choices hidden under the pinned action bar must be scrolled clear first (`tapAboveBar`).
    - On iOS 26+, `confirmationDialog` is a popover with no Cancel button; dismiss it by tapping `PopoverDismissRegion`.
    - The audit fails contrast for content scrolled under the glass navigation bar, so `audit()` scrolls to the top first.
    - Landscape (`LandscapeTests`): the short height cuts choices in half under the opaque pinned bar, and the audit flags that as contrast; `audit(ignoringCutByBar: true)` ignores only elements crossing the top edge of `actionBar` (the bar container), so the bar's own buttons are still audited.
    - Text in rows must wrap (`fixedSize(horizontal: false, vertical: true)`, vertical-axis `TextField`), or the audit reports "Text clipped".

## Commands
Android (use Android Studio's JBR; CI uses Temurin 17):
```sh
# Set JAVA_HOME to a JDK 17+ (Android Studio's bundled JBR works)
cd android && ./gradlew test koverVerify koverHtmlReport      ./gradlew lint assembleDebug
# Emulator (local AVD Pixel_6, API 36; CI uses API 34): end-to-end flows with ATF checks + UI-only gate
$ANDROID_HOME/emulator/emulator -avd Pixel_6 -no-window -no-audio &   # wait for sys.boot_completed
./gradlew connectedDebugAndroidTest createDebugAndroidTestCoverageReport uiCoverageVerify
```
iOS:
```sh
cd ios/Packages/CheckInCore && swift test                      # fast local loop
xcodebuild test -scheme CheckInCore-Package -destination 'platform=macOS' \
  -enableCodeCoverage YES -resultBundlePath ../../build/CoreTests.xcresult
cd ../../.. && python3 scripts/check_ios_coverage.py ios/build/CoreTests.xcresult 80
# App (scheme CheckInSOS also runs the CheckInCore package tests). Local simulator: iPhone 17, iOS 27.0.
cd ios && xcodegen generate
D='platform=iOS Simulator,name=iPhone 17,OS=27.0'
xcodebuild test -project CheckInSOS.xcodeproj -scheme CheckInSOS -destination "$D" -derivedDataPath build/DerivedData \
  -skip-testing:CheckInSOSUITests -enableCodeCoverage YES -resultBundlePath build/UnitTests.xcresult CODE_SIGNING_ALLOWED=NO
xcodebuild test -project CheckInSOS.xcodeproj -scheme CheckInSOS -destination "$D" -derivedDataPath build/DerivedData \
  -only-testing:CheckInSOSUITests -enableCodeCoverage YES -resultBundlePath build/UITests.xcresult CODE_SIGNING_ALLOWED=NO
xcrun xcresulttool merge build/UnitTests.xcresult build/UITests.xcresult --output-path build/All.xcresult
cd .. && python3 scripts/check_ios_coverage.py ios/build/UITests.xcresult 80 --scope presentation
python3 scripts/check_ios_coverage.py ios/build/All.xcresult 80
```

## Coverage gates (never lower 80% silently)
- **Overall, ≥80% line coverage, all layers.**
  - Android: Kover is aggregated in the **root** project (`android/build.gradle.kts`, `kover(project(":domain"|":data"|":app"))`).
    - Kover's total variant skips Android build variants. Every module therefore adds a custom `all` variant (`jvm` for domain/data, `debug` for app), and the root `koverVerify` / `koverHtmlReport` depend on `koverVerifyAll` / `koverHtmlReportAll`.
  - iOS: `scripts/check_ios_coverage.py` reads the merged xcresult.
- **UI tests only, ≥80% of the presentation layer.**
  - iOS: `--scope presentation`, covering `Features/`, `DesignSystem/` and `App/AppRouter.swift`.
  - Android: JaCoCo `uiCoverageVerify` over `feature.**`, `designsystem.**` and `navigation.**` (`app/build.gradle.kts`; report in `app/build/reports/jacoco/uiCoverageReport`).
    - Robots and flows live in `app/src/sharedTest` and are compiled into both `test` (Robolectric, which counts for Kover) and `androidTest` (emulator, JaCoCo and ATF).
- **Exclusions** (keep these lists in sync: `EXCLUDED_FILES` in the script, and `kover { reports { filters } }`):
  - The app entry point (`CheckInSOSApp`, `MainActivity`).
  - Compose-generated `*ComposableSingletons*` classes (Android).
  - `AppContainer`.
  - `#Preview` / `@Preview` (plus the `Previews/` folder on iOS and the `uk.checkinsos.app.previews` package on Android).
  - Test fakes.
  - Every addition must be documented in the README ("Coverage exclusions" table).
- Current status: iOS overall 99.4% and UI-only presentation 98.7%; Android overall (domain + data + app) 99.7% and UI-only presentation (emulator) 97.4%.
- Android test counts: 34 ViewModel tests, 12 Robolectric screen tests and 20 shared flows (run on both the JVM and the emulator, with ATF), 3 JVM-only landscape/rotation tests, plus the domain and data tests.
- iOS test counts: 36 ViewModel tests, 26 domain, 6 data, and 26 XCUITests (6 of them run `performAccessibilityAudit()`, 3 run in landscape).

## Status
- [x] **Phase 1, foundation:**
  - git, `.gitignore`, this file, `docs/screens.md`.
  - The iOS `CheckInCore` and Android `:domain`/`:data` modules, with tests.
  - CI (`.github/workflows/ios.yml`, `android.yml`) running unit tests plus the overall gate.
- [x] **Phase 2, iOS app:**
  - `ios/project.yml` (XcodeGen; app, unit-test and UI-test targets; local package).
  - `AppContainer`, `AppRouter`, the design system, and every screen.
  - ViewModel tests, XCUITest robots and flows, `performAccessibilityAudit()`.
  - Extend `ios.yml`.
- [x] **Phase 3, Android app:**
  - `:app` module (AGP 9 built-in Kotlin plus the `kotlin-compose` plugin; minSdk 26, compile/target 36).
  - Screens, ViewModel tests, Robolectric screen tests, and `androidTest` flows with ATF checks.
  - JaCoCo UI gate and the `ui-tests` CI job.
- [x] **Phase 4, hardening:** coverage exclusions, README, and an end-to-end walkthrough on both platforms.

## Toolchain (pinned)
- Gradle 9.4.0 (wrapper), AGP 9.0.1 (built-in Kotlin), Kotlin 2.3.20, Kover 0.9.9, coroutines 1.10.2, JUnit 4 + kotlin-test. Versions live in `android/gradle/libs.versions.toml`.
- Android app: compileSdk/targetSdk 36 and minSdk 26.
  - Libraries: Compose BOM 2026.06.01 (Compose 1.11.4), Navigation 2.9.8, Lifecycle 2.10.0, Robolectric 4.18.1 and JaCoCo 0.8.13.
  - Compose 1.12, Navigation 2.10 and Lifecycle 2.11 need compileSdk 37 plus AGP 9.1, so bump all of these together.
  - Kover 0.9.5+ is required for AGP 9 build variants.
- Swift tools 6.0 (strict concurrency) with Swift Testing. The package targets iOS 17 and macOS 14; macOS is only there for fast `swift test` runs.
