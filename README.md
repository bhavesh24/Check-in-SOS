# Check-in SOS

Two native apps built from one design: **iOS** (SwiftUI) and **Android** (Jetpack Compose). Check-in SOS
lets an older adult do a daily wellbeing check-in. A low score, or a missed check-in, alerts their
neighbour, who can escalate to emergency services. Family members follow a timeline, and hospitals
record the care outcome.

This build covers **only the screens after sign-in**, for four roles: older adult, concerned family,
neighbour and hospital. A role picker stands in for login. Everything runs on **mock data**: there is
no backend, no authentication and no real notifications.

Both apps show the same copy, mock data, test IDs and behaviour. The single source of truth is
[`docs/screens.md`](docs/screens.md). Day-to-day working notes for contributors (and Claude) are in
[`CLAUDE.md`](CLAUDE.md).

## What's in the apps

| Role | Flow |
|---|---|
| Home | Role picker: "Old age adult" / "Concerned family" / "Hospital" / "Neighbour" |
| Older adult | Assessment (physical + mood) → Score (below 70: "A notification has been sent to your neighbour") · demo "missed check-in" screen |
| Concerned family | Loved ones → Current status (score + 1st-level neighbour) → Edit neighbour (validated) · Timeline |
| Neighbour | Neighbours → *Simulate alert* → Help (Accept + countdown) → Have you reached? → Escalate? → 999/111 → Is your neighbour alive? |
| Hospital | Patients → *Simulate alert* → Help → In care? → Recovering? → Recovered? / Bad condition? → Is the patient alive? |

Every screen has a back control. Multi-step flows show
"Step N of M". The teal theme is darkened to pass WCAG AA, touch targets are at least 48 pt/dp, text
scales with the system font size, and dark mode is supported.

### What is mocked
- **Data:** an in-memory `MockDataSource` seeded with three older adults (Margaret Wilson 76, Arthur
  Patel 38, Edith Clarke with no score yet), three hospital patients and a sample timeline. Changes
  last until the app is killed.
- **Push notifications:** replaced by a **Simulate alert** button on the Neighbours and Patients lists.
  It raises an alert about the lowest-scoring person (Arthur Patel, with a 20-minute deadline).
- **Calling emergency services:** **999 / 111 never dial.** The next screen shows "Demo: would call
  999" instead.
- **Scoring:** `ScoreCalculator` is a **placeholder** formula, because none is specified yet.
  It reproduces the prototype's examples (Good + Pleasant = 76, Ill + Anxious = 38).
- **Test mode:** fresh data, a fixed clock (2026-01-15 09:00 UTC) and UTC times. This is the
  `-ui-testing` launch argument on iOS and `AppContainer.forTesting()` on Android.

## Repository layout

```
docs/screens.md                 screen inventory, copy, test IDs, mock data, domain rules
ios/
  project.yml                   XcodeGen spec (app, unit-test and UI-test targets; local package)
  Packages/CheckInCore/         Domain + Data (Swift package) with tests
  CheckInSOS/                   Presentation: App/, DesignSystem/, Features/<Role>/<Screen>/, Previews/
  CheckInSOSTests/              ViewModel tests (Swift Testing, fake repositories)
  CheckInSOSUITests/            XCUITest robots + per-role flows + accessibility audit
android/
  domain/  data/                pure Kotlin/JVM modules with tests
  app/src/main/                 Presentation: navigation/, designsystem/, feature/<role>/<screen>/
  app/src/test/                 ViewModel tests + Robolectric screen tests
  app/src/sharedTest/           robots + end-to-end flows, run on the JVM *and* on the emulator
scripts/check_ios_coverage.py   iOS coverage gate
.github/workflows/              ios.yml, android.yml
```

## Architecture

Both apps use Clean Architecture with MVVM. Dependencies point inward, and module boundaries enforce
this:

| Layer | iOS | Android | Depends on |
|---|---|---|---|
| Domain: entities, repository interfaces, use cases, `ScoreCalculator`, `EscalationPolicy` | `CheckInCore/Domain` | `:domain` | nothing |
| Data: `MockDataSource` + repository implementations | `CheckInCore/Data` | `:data` | Domain |
| Presentation: one View + ViewModel per screen, design system, navigation | app target | `:app` | Domain (Data only in `AppContainer`) |

- **Use cases** do one action each (`SubmitAssessment`, `SimulateAlert`, `RecordCareOutcome`, …).
- **ViewModels never navigate.** They report outcomes, and the router maps those to screens:
  - iOS: closures, with `AppRouter` and its `[Route]` path.
  - Android: one-off events, with a Navigation Compose `NavHost` and type-safe routes.
- **Dependency injection:** a manual composition root (`AppContainer`) wires everything by
  constructor injection. There's no DI framework.
- **Swapping in a real backend** means writing new Data-layer repositories. Domain and Presentation
  don't change.

## Running the apps

### iOS (Xcode 16 or later, built with Xcode 27; iOS 17+ simulator)
```sh
brew install xcodegen          # once
cd ios && xcodegen generate    # creates CheckInSOS.xcodeproj (git-ignored)
open CheckInSOS.xcodeproj      # run the CheckInSOS scheme on an iPhone simulator
```
To run on your own iPhone, select your personal team (a free Apple ID works) under *Signing &
Capabilities*. Apps signed this way expire after 7 days.

### Android (Android Studio with its bundled JBR, SDK 36)
```sh
cd android
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # onto a running emulator or connected device
```
Or open `android/` in Android Studio and run the `app` configuration. The debug APK installs on any
Android 8.0+ (API 26) phone.

## Tests and coverage gates

Every interactive element has the test ID listed in `docs/screens.md`. UI tests use **robot
(page-object)** helpers, so each flow reads like the user journey it tests.

| | iOS | Android |
|---|---|---|
| Domain + data unit tests | `swift test` (Swift Testing) | `./gradlew :domain:test :data:test` |
| ViewModel tests (fake repositories) | `CheckInSOSTests` | `app/src/test/.../viewmodel` |
| Screen tests from a fixed UI state | (covered by the UI tests) | Robolectric `ScreenContentTest` |
| End-to-end flows per role, back navigation, step progress | XCUITest (`CheckInSOSUITests`) | `sharedTest` flows on Robolectric **and** the emulator |
| Accessibility | `performAccessibilityAudit()` on every role's main screens | Accessibility Test Framework checks on every emulator action |

Two **80% line-coverage gates** run on each platform. Neither threshold may be lowered silently.

| Gate | iOS | Android | Current |
|---|---|---|---|
| Overall (all tests, all layers) | merged `All.xcresult` + `scripts/check_ios_coverage.py` | Kover `koverVerify` (merged `all` variant: `:domain`, `:data`, `:app` debug) | iOS 99.4%, Android 99.7% |
| UI tests only, presentation layer | `UITests.xcresult` + `--scope presentation` | JaCoCo `uiCoverageVerify` (emulator) | iOS 98.7%, Android 97.4% |

The exact commands are in [`CLAUDE.md`](CLAUDE.md#commands). In short:
```sh
# iOS (from ios/, after xcodegen generate): unit run, UI run, merge, then both gates
# Android (from android/)
./gradlew lint test koverVerify koverHtmlReport assembleDebug
./gradlew connectedDebugAndroidTest createDebugAndroidTestCoverageReport uiCoverageVerify   # needs an emulator
```

### Coverage exclusions
These are the only files and classes left out of the gates. The lists live in
`scripts/check_ios_coverage.py` (`EXCLUDED_FILES`, `EXCLUDED_PATH_PARTS`), `android/build.gradle.kts`
(`kover { reports { filters } }`) and `android/app/build.gradle.kts` (`uiCoverageClasses`). Keep them
in sync with this list.

| Excluded | Why |
|---|---|
| `CheckInSOSApp.swift`, `MainActivity` | App entry points: they only start the root view |
| `AppContainer` (both) | Composition root: wiring only, exercised by every UI test |
| `ios/CheckInSOS/Previews/`, `uk.checkinsos.app.previews.*`, `@Preview` functions | SwiftUI/Compose previews, never run in tests |
| Test fakes (`/Fakes/`) and test targets | Test-only code |
| `*ComposableSingletons*`, `*$inlined*` (Android) | Compose-compiler and Kotlin-generated classes with no source lines of their own |
| `/.build/`, `/checkouts/` (iOS) | SwiftPM build products and third-party checkouts |

## CI/CD (GitHub Actions)

There's no deployment: nothing is signed, uploaded or published. The pipelines only build and test.

- **`.github/workflows/ios.yml`**
  - Runs on macOS, on changes under `ios/**`.
  - Steps: `swift test`, then `xcodegen generate`, then the unit tests and the UI tests on the newest
    available iPhone simulator, then the UI-only gate, then a merge and the overall gate.
  - Uploads the `.xcresult` bundles, which include screenshots of failures.
- **`.github/workflows/android.yml`**, on changes under `android/**`:
  - `build-test` (Ubuntu): `lint test koverVerify koverHtmlReport assembleDebug`. Uploads the APK and
    the test, lint and Kover reports.
  - `ui-tests` (Ubuntu + KVM, API 34 emulator via `reactivecircus/android-emulator-runner`):
    `connectedDebugAndroidTest createDebugAndroidTestCoverageReport uiCoverageVerify`. Uploads the
    instrumented test reports and the JaCoCo report.

GitHub Actions is free for public repositories. Private repositories get a monthly minutes allowance,
and macOS minutes use it up about 10× faster. The iOS UI tests take about 15 minutes.

## Platform differences (by design)

| | iOS | Android |
|---|---|---|
| Back | Navigation-bar back button (`nav.back`), plus the edge swipe | Top-app-bar up button (`nav.back`), plus system back |
| Emergency choice | Action sheet. On iOS 26+ it's a popover with no Cancel button (tap outside to dismiss) | Material dialog with Cancel / 999 / 111 |
| Keyboard | "Done" button above the keyboard (`keyboard.done`), because the phone pad has no return key | The keyboard's own Next/Done action |
| Screen title in tests | Navigation bar title | `screen.title` tag |

## Open questions

These rules are specified but not implemented in this build yet (details in
[`docs/screens.md`](docs/screens.md)):
- A check-in counts as missed after **24 hours**, and the 1st-level neighbour is notified.
- If the neighbour doesn't acknowledge in time, the alert escalates to the empanelled hospitals.
- Family members get an update on every health change, and whenever a level misses its notification.

Still open:
1. **Scoring formula.** The formula is a placeholder; its thresholds and weights need clinical input.
2. **Death-notification consent.** Who may see "Reported deceased", and how is the family told?
3. **Acknowledgement deadline for a missed check-in.** Deadlines are specified only by score band,
   and a missed check-in has no score. This uses 30 minutes as a placeholder.
4. **"Not in bad condition" / "not yet recovered".** The prototype doesn't say what to record, so
   these return to the list without recording an outcome.

## Toolchain
- **iOS:** Swift 6 (strict concurrency), SwiftUI with Observation, iOS 17+, XcodeGen.
- **Android:** Gradle 9.4, AGP 9.0.1 (built-in Kotlin), Kotlin 2.3.20, Compose BOM 2026.06.01,
  Navigation 2.9.8, Kover 0.9.9, Robolectric 4.18.1, JaCoCo 0.8.13. minSdk 26, compile/target SDK 36.
- Versions are pinned in `android/gradle/libs.versions.toml`. Compose 1.12, Navigation 2.10 and
  Lifecycle 2.11 need compileSdk 37 and AGP 9.1, so upgrade all of these together.
