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
  - `EscalationPolicy`: 50–69 → 30 min, 30–49 → 20 min, 0–29 → 15 min, missed check-in → 30 min (placeholder).
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
- **DI:** a manual composition root (`AppContainer`) wires everything through constructor injection; there's no DI framework.
  - Test mode uses fresh mock data and `FixedTimeProvider(fixedNow)`. On iOS that's the `-ui-testing` launch argument; on Android it's a test `AppContainer`.
- **UI conventions:** every interactive element gets the test ID listed in `docs/screens.md`, via `accessibilityIdentifier` on iOS and `Modifier.testTag` on Android.
  - UI tests use robot (page-object) helpers.
  - The 999/111 buttons must **never** dial; they show "Demo: would call 999" instead.

## Commands
Android (use Android Studio's JBR; CI uses Temurin 17):
```sh
# Set JAVA_HOME to a JDK 17+ (Android Studio's bundled JBR works)
cd android && ./gradlew test koverVerify koverHtmlReport      # phase 3 adds: lint assembleDebug
# phase 3 emulator: ./gradlew connectedDebugAndroidTest createDebugAndroidTestCoverageReport uiCoverageVerify
```
iOS:
```sh
cd ios/Packages/CheckInCore && swift test                      # fast local loop
xcodebuild test -scheme CheckInCore-Package -destination 'platform=macOS' \
  -enableCodeCoverage YES -resultBundlePath ../../build/CoreTests.xcresult
cd ../../.. && python3 scripts/check_ios_coverage.py ios/build/CoreTests.xcresult 80
# phase 2: cd ios && xcodegen generate; unit run (-skip-testing:CheckInSOSUITests) → build/UnitTests.xcresult;
#          UI run (-only-testing:CheckInSOSUITests) → build/UITests.xcresult;
#          python3 scripts/check_ios_coverage.py ios/build/UITests.xcresult 80 --scope presentation;
#          xcrun xcresulttool merge both → build/All.xcresult; check_ios_coverage.py ios/build/All.xcresult 80
```

## Coverage gates (never lower 80% silently)
- **Overall, ≥80% line coverage, all layers.**
  - Android: Kover is aggregated in the **root** project (`android/build.gradle.kts`, `kover(project(":domain"|":data"))`). Phase 3 must add `kover(project(":app"))`.
  - iOS: `scripts/check_ios_coverage.py` reads the merged xcresult.
- **UI tests only, ≥80% of the presentation layer.**
  - iOS: `--scope presentation`, covering `Features/`, `DesignSystem/` and `App/AppRouter.swift`.
  - Android: JaCoCo `uiCoverageVerify` over `feature.**`, `designsystem.**` and `navigation.**`.
- **Exclusions** (keep these lists in sync: `EXCLUDED_FILES` in the script, and `kover { reports { filters } }`):
  - The app entry point (`CheckInSOSApp`, `MainActivity`).
  - `AppContainer`.
  - `#Preview` / `@Preview`.
  - Test fakes.
  - Every addition must be documented in the README.
- Current status: iOS core 100%, Android domain+data 98.9%.

## Status
- [x] **Phase 1, foundation:**
  - git, `.gitignore`, this file, `docs/screens.md`.
  - The iOS `CheckInCore` and Android `:domain`/`:data` modules, with tests.
  - CI (`.github/workflows/ios.yml`, `android.yml`) running unit tests plus the overall gate.
- [ ] **Phase 2, iOS app:**
  - `ios/project.yml` (XcodeGen; app, unit-test and UI-test targets; local package).
  - `AppContainer`, `AppRouter`, the design system, and every screen.
  - ViewModel tests, XCUITest robots and flows, `performAccessibilityAudit()`.
  - Extend `ios.yml`.
- [ ] **Phase 3, Android app:**
  - `:app` module (AGP 9 built-in Kotlin plus the `kotlin-compose` plugin; minSdk 26, compile/target 36).
  - Screens, ViewModel tests, Robolectric screen tests, and `androidTest` flows with ATF checks.
  - JaCoCo UI gate and the `ui-tests` CI job.
- [ ] **Phase 4, hardening:** coverage exclusions, README, and an end-to-end walkthrough on both platforms.

## Toolchain (pinned)
- Gradle 9.4.0 (wrapper), AGP 9.0.1, Kotlin 2.3.20, Kover 0.9.4, coroutines 1.10.2, JUnit 4 + kotlin-test. Versions live in `android/gradle/libs.versions.toml`.
- Swift tools 6.0 (strict concurrency) with Swift Testing. The package targets iOS 17 and macOS 14; macOS is only there for fast `swift test` runs.
