# Check-in SOS — screen inventory (single source of truth)

Both apps (iOS + Android) mirror this file exactly: copy, flow, test IDs and mock data.
If a screen changes, change it here first, then in both apps.

**Out of scope (login/onboarding):** enrollment "Do you have a code?" (older adult, neighbour), hospital
empanelment code, family registration wizard + "All Done!" confirmation. The role picker is the demo entry point.

## Global rules
- Every screen except Home has a back control (iOS nav-bar back, Android top-app-bar up + system back). Test ID `nav.back`.
- Answer buttons: "Yes" (or the "Yes, …" choice) is the filled primary button, and other answers are outlined. Answers keep the order listed per screen.
- Platform-only test IDs: `screen.title` is the Android top-app-bar title (iOS tests read the navigation bar), `keyboard.done` is the iOS keyboard toolbar button (Android uses the IME action), and `actionBar` is the iOS pinned action bar container (used by the landscape audit).
- Multi-step flows show a step indicator "Step N of M" (test ID `stepProgress`). Step counts are listed per flow below.
- Theme: teal, **darkened** so white-on-teal passes WCAG AA (≥4.5:1). Touch targets ≥ 48×48 pt/dp; primary buttons ≥ 56 high, full width.
- Text scales with Dynamic Type (iOS) / font scale (Android); no fixed-height text containers.
- Orientation: both apps support all four orientations: portrait, upside-down portrait and landscape left/right (Android `fullUser`, so the user's rotation lock is respected). iPhones with Face ID never rotate upside-down (an iOS restriction), so there it only applies to Home-button iPhones. At short landscape heights content scrolls under the pinned action bar rather than clipping.
- Push notifications are **simulated** by a "Simulate alert" button. 999/111 **never dials** in this build.
- Any unexpected failure loading or saving shows "Something went wrong. Please try again." (test ID `error.message`) on the current screen.
- Lists (Loved ones, Neighbours, Patients) are one grouped panel (`surfaceTint`, rounded) with a divider
  between rows, like an iOS inset grouped list. Each row starts with an initials avatar (first letters of the
  first and last name, e.g. "MW", in `onPrimary` on a `primary` circle) and ends with a score pill: the number on a tint of its score band colour
  (see Score), or "No score" when there is none. The pill's accessibility text is "Score 76" / "No score yet".
- Haptics: primary and secondary buttons give a light tap on press (iOS `sensoryFeedback`, Android
  `performHapticFeedback`); answer tiles give a selection tick.
- "When" text (`DisplayText.checkIn`): "Today, 08:00", "Yesterday, 08:00", otherwise "13 Jan, 08:00"
  (day and short month), all relative to the injected clock.
- Times (timeline `HH:mm`) use the device time zone; in UI-test mode they use UTC so the fixed clock gives stable values (e.g. `oa-2` "Score 38" at 06:00).
- UI-test launch: iOS launch argument `-ui-testing`; Android test `AppContainer`. Both use fresh mock data and the fixed clock below.

## Design tokens
| Token | Light | Dark | Notes |
|---|---|---|---|
| `primary` (buttons, titles) | `#0F766E` | `#2DD4BF` | white on `#0F766E` = 5.5:1 (AA). Dark mode uses `onPrimary #042F2E` |
| `onPrimary` | `#FFFFFF` | `#042F2E` | |
| `background` | `#FFFFFF` | `#0B1214` | |
| `surfaceTint` (list rows) | `#F0FDFA` | `#132326` | |
| `statusCritical` (red) | `#B42318` | `#F97066` | timeline: score <70, notification sent, escalation |
| `statusWarning` (amber) | `#B54708` | `#FDB022` | timeline: acknowledged, in hospital care |
| `statusOk` (green) | `#067647` | `#47CD89` | timeline: neighbour reached, recovered, alive, all OK |
| Score numeral | 72 pt/sp, primary | | rendered as `76/` newline `100` like the prototype (Current status) |
| `roleOlderAdult` | `#0F766E` | `#2DD4BF` | Home card icon badge (same as `primary`) |
| `roleFamily` | `#6941C6` | `#B692F6` | Home card icon badge |
| `roleHospital` | `#175CD3` | `#84ADFF` | Home card icon badge |
| `roleNeighbour` | `#B93815` | `#F38744` | Home card icon badge |
| `onRole` | `#FFFFFF` | `#0B1214` | icon on a role badge (≥4.5:1 on every role colour) |
| `cardBorder` | `#E4E7EC` | `#344054` | 1 pt/dp outline of Home cards |
| `scoreMild` (50–69) | `#F04438` | `#FDA29B` | Score ring, lightest red |
| `scoreModerate` (30–49) | `#D92D20` | `#F97066` | Score ring |
| `scoreSevere` (0–29) | `#912018` | `#F04438` | Score ring, deepest red. Every ring colour is ≥3:1 on `background` |
| `urgent` banner | `statusCritical` | `statusCritical` | alert banner background; text/icon `onRole` (≥4.5:1) |

**Icons** are decorative (hidden from accessibility) and platform-native: SF Symbols on iOS, Material icons on Android.

| Item | SF Symbol | Material icon |
|---|---|---|
| Old age adult | `person.fill.checkmark` | `HowToReg` |
| Concerned family | `person.2.fill` | `Groups` |
| Hospital | `cross.case.fill` | `LocalHospital` |
| Neighbour | `house.fill` | `Home` |
| Fine / Good / Ill / Very sick | `hand.thumbsup.fill` / `star.fill` / `thermometer.medium` / `bed.double.fill` | `ThumbUp` / `Star` / `Thermostat` / `Bed` |
| Happy / Pleasant / Anxious / Depressed | `sun.max.fill` / `cloud.sun.fill` / `cloud.bolt.rain.fill` / `cloud.rain.fill` | `WbSunny` / `WbCloudy` / `Thunderstorm` / `Umbrella` |

## Fixed values for tests
- Fixed clock ("now"): **2026-01-15T09:00:00Z**.
- Current older adult (older-adult role): `oa-1`.

## Mock data (seeded by `MockDataSource`)
Older adults (family "loved ones" and neighbour "neighbours" lists both show these three, in this order):

| id | Name | Latest score | 1st-level neighbour (first / middle / last / email / mobile) |
|---|---|---|---|
| `oa-1` | Margaret Wilson | 76 | Alex / — / Brown / alex.brown@example.com / 07700900123 |
| `oa-2` | Arthur Patel | 38 | Priya / — / Shah / priya.shah@example.com / 07700900456 |
| `oa-3` | Edith Clarke | — (none yet) | Tom / J / Hughes / tom.hughes@example.com / 07700900789 |

Hospital patients (in this order): `p-1` Arthur Patel (→ `oa-2`), `p-2` Edith Clarke (→ `oa-3`), `p-3` Margaret Wilson (→ `oa-1`).

Seeded timeline for `oa-2` (mirrors the prototype timeline; times relative to the fixed clock):

| # | Offset | Event | Copy | Colour |
|---|---|---|---|---|
| 0 | −3h00 | score(38) | "Score 38" | critical |
| 1 | −2h59 | notificationSent(level 1) | "Notification sent to 1st level" | critical |
| 2 | −2h50 | acknowledged(level 1) | "1st level acknowledged" | warning |
| 3 | −2h35 | neighbourReached | "Neighbour reached" | ok |
| 4 | −2h30 | escalated(level 2) | "Escalation to 2nd level" | critical |
| 5 | −2h20 | acknowledged(level 2) | "2nd level acknowledged" | warning |
| 6 | −1h30 | careOutcome(inCare) | "In care in hospital" | warning |
| 7 | −0h10 | careOutcome(recovered) | "Recovered" | ok |

`oa-1` has one event: score(76) at −1h00. `oa-3` has none ("No events yet").

Seeded score history (`GetScoreHistory`; one check-in a day, oldest first, the last one at the time of the
adult's "Score N" timeline event, so it sets their last check-in):

| id | Scores (6 days ago → today) | Last check-in |
|---|---|---|
| `oa-1` | 72, 70, 74, 68, 73, 78, 76 | −1h00 (08:00) |
| `oa-2` | 74, 71, 66, 60, 52, 45, 38 | −3h00 (06:00) |
| `oa-3` | none | — |

`SubmitAssessment` records each new score with the current time, updating the latest score, last check-in
and history.

## Domain rules
**ScoreCalculator (PLACEHOLDER: no formula is specified).** Score = physical + mood, 0–100.
Chosen so the prototype's examples reproduce: Good + Pleasant = **76**, Ill + Anxious = **38**.

| Physical | pts | Mood | pts |
|---|---|---|---|
| Fine | 50 | Happy | 50 |
| Good | 38 | Pleasant | 38 |
| Ill | 20 | Anxious | 18 |
| Very sick | 5 | Depressed | 5 |

- Score **≥ 70** = good: no notification. Score **< 70**: the 1st-level neighbour is notified.

**EscalationPolicy:** the acknowledgement timeout depends on the score.
- 70–100: no alert
- 50–69: 30 min
- 30–49: 20 min
- 0–29: 15 min
- Missed check-in (no score): 15 min. No acknowledgement deadline is specified for an alert without a score, so this is a placeholder; see open questions.

The alert countdown counts down to `raisedAt + timeout`, shown as `mm:ss`.

Escalation levels: **1** = neighbour, **2** = emergency services / hospital.

**Specified rules (not yet built in either app):**
- **Missed check-in threshold: 24 hours.** If the older adult hasn't taken the assessment for 24 hours,
  the 1st-level neighbour is notified, exactly as for a score below 70. The alert goes to the
  neighbour, not to the family.
- **Unacknowledged alert.** If the neighbour doesn't acknowledge before the deadline, the alert goes to
  the empanelled hospitals (2nd level).
- **Family updates.** Registered family members receive an update about every change to the older
  adult's health, including a missed check-in, and an update whenever a level misses its
  notification (the neighbour at level 1, or no hospital responding at level 2).

In this build the threshold isn't modelled (No check-in is a demo link), the countdown is
display-only, and family see events only on the Timeline.

## Screens

### Home: role picker
- Title: "Home". Heading: "Are you".
- One full-width card per role, in this order. Each card is a single button: a circular icon badge in the
  role's colour, the label, the description underneath, and a trailing chevron. Its accessibility label is
  the label, and its hint is the description.

| Label | Description | Test ID | Goes to |
|---|---|---|---|
| "Old age adult" | "Do your daily check-in" | `home.role.olderAdult` | Assessment |
| "Concerned family" | "See how your loved ones are doing" | `home.role.family` | Loved ones |
| "Hospital" | "Respond to patient alerts" | `home.role.hospital` | Patients |
| "Neighbour" | "Help someone living nearby" | `home.role.neighbour` | Neighbours |

### Older adult (2 steps: Assessment = 1, Score = 2)
**Assessment**
- Title "Assessment".
- Answers are tiles in a 2-column grid (1 column when text is very large): an icon above the label.
  Unselected tiles are outlined; the selected tile is filled `primary` with a checkmark.
- Section "How do you feel physically?", choose one:
  - "Fine" `assessment.physical.fine`
  - "Good" `assessment.physical.good`
  - "Ill" `assessment.physical.ill`
  - "Very sick" `assessment.physical.verySick`
- Section "How is your mood?", choose one:
  - "Happy" `assessment.mood.happy`
  - "Pleasant" `assessment.mood.pleasant`
  - "Anxious" `assessment.mood.anxious`
  - "Depressed" `assessment.mood.depressed`
- "Submit" `assessment.submit` is disabled until both sections are answered. It runs `SubmitAssessment` and goes to Score.
- Demo link "Demo: missed check-in" `assessment.demoNoCheckIn` goes to No check-in.

**Score**
- Title "Score".
- Score ring `score.value`: a circular gauge filled to score/100, with the number in the middle and
  "out of 100" under it (accessibility label "Score 76 out of 100"). The ring colour follows the
  EscalationPolicy bands: `statusOk` (green) for ≥ 70, then shades of red that deepen as the score drops:
  `scoreMild` for 50–69, `scoreModerate` for 30–49 and `scoreSevere` for 0–29. The number stays in the
  text colour.
- If the score is ≥ 70: "You're doing well today." `score.reassurance`.
- If the score is < 70: "A notification has been sent to your neighbour" `score.neighbourNotified`.
- "Done" `score.done` returns to Home.

**No check-in** (registered but not checked in after the threshold time)
- Title "No Check-in".
- Message "As you have not checked in, a notification has been sent to your neighbour" `noCheckIn.message`.
- Back only.

### Concerned family
**Loved ones**
- Title "Your loved ones".
- Rows (grouped list, see Global rules) show the avatar, the name, "Last check-in Today, 08:00" (or
  "No check-in yet"), the score pill and a chevron. The row's text includes "Score 76" / "No score yet".
- Test ID `lovedOnes.row.<id>`. A row opens Current status.

**Current status**
- Title = the person's name.
- Caption "Current score", then the Score ring `status.score` (same colours as the Score screen, 160 pt/dp;
  label "Score 76 out of 100"). With no score yet it shows "—" (label "No score yet").
- "Last check-in Today, 08:00" `status.lastCheckIn`, or "No check-in yet".
- Neighbour card: section caption "1st level neighbour", initials avatar, "1st level: Alex Brown"
  `status.neighbour`, and a pencil button `status.editNeighbour` (label "Edit neighbour") that goes to Edit neighbour.
- Section "Recent check-ins" `status.trend`: a line chart of `GetScoreHistory` (the last 7 check-ins, oldest
  first) on a 0–100 axis, with a dashed line at 70. Shown only with 2 or more check-ins. Accessibility label
  "Recent check-ins: 72, 70, 74, 68, 73, 78, 76".
- "Timeline" `status.timeline` goes to Timeline.

**Edit neighbour**
- Title "Edit Neighbour". Heading "Your 1st level notified person".
- Fields, pre-filled:
  - First name* `editNeighbour.firstName`
  - Middle name `editNeighbour.middleName`
  - Last name* `editNeighbour.lastName`
  - Email* `editNeighbour.email`
  - Mobile* `editNeighbour.mobile`, after a fixed "+44" prefix. The field shows the stored number without its leading "0" ("07700900123" → "7700900123"); on save a "0" is put back unless the text already starts with "0" or "+".
- A "Done" button above the keyboard `keyboard.done` closes it (the phone pad has no return key).
- "Save" `editNeighbour.save` runs `UpdateNeighbour`, then goes back to Current status, which shows the new name.
- Validation errors appear in `editNeighbour.error`, under the heading and above the fields so they stay visible while the keyboard is open:
  - "First name is required"
  - "Last name is required"
  - "Enter a valid email"
  - "Enter a valid UK mobile number" (after removing spaces/dashes and a leading "+44" or "0", 9–10 digits must remain)

**Timeline**
- Title "Timeline".
- A vertical list of events, oldest first. Each row has an icon badge (the event's colour, icon in `onRole`),
  the copy and the time (HH:mm). A 2 pt/dp line joins the badges. Icons are decorative:

| Event | SF Symbol | Material icon |
|---|---|---|
| Score | `heart.text.square.fill` | `MonitorHeart` |
| Missed check-in | `clock.badge.exclamationmark` | `AlarmOff` |
| Notification sent | `bell.fill` | `NotificationsActive` |
| Acknowledged | `hand.raised.fill` | `PanTool` |
| Neighbour reached | `figure.walk` | `DirectionsWalk` |
| Escalation | `exclamationmark.triangle.fill` | `Warning` |
| In care | `cross.case.fill` | `LocalHospital` |
| Recovering | `arrow.up.heart.fill` | `Healing` |
| All OK / Recovered / Confirmed alive | `checkmark.seal.fill` | `Verified` |
| In bad condition | `exclamationmark.triangle.fill` | `Warning` |
| Reported deceased | `heart.slash.fill` | `HeartBroken` |
- Test ID `timeline.event.<index>`. When there are no events: "No events yet" `timeline.empty`.

### Neighbour (4 steps: Alert = 1, Reached = 2, Escalate = 3, Alive = 4)
**Neighbours list**
- Title "Neighbours". Rows `neighbours.row.<id>` (grouped list: avatar, name, score pill) are informational.
- "Simulate alert" `neighbours.simulateAlert` runs `SimulateAlert(neighbour)`. It picks the lowest-scoring older adult (Arthur Patel, 38, so the timeout is 20 min) and goes to Help alert.

**Help alert** (shared with Hospital alert)
- Title "Urgent alert".
- A full-width urgent banner: warning icon and "Urgent: someone needs help now", on `statusCritical`.
- "Your neighbour Arthur Patel needs your help, please acknowledge the notification" `alert.message`.
- Countdown ring: a `statusCritical` ring that empties as time runs out (full when the screen opens), with
  the countdown `alert.countdown` in the middle, e.g. "20:00", ticking each second, and "left to respond"
  under it. Display only; on expiry it shows "Time expired" and the ring is empty.
- "Accept" `alert.accept` runs `AcknowledgeAlert` and goes to Reached.

**Have you reached?**
- Title "On your way".
- "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached" `reached.message`.
- "I have reached" `reached.confirm` runs `MarkArrived` and goes to Escalate.

**Escalate**
- Title "Escalate". Heading "Do you want to escalate?".
- "No, All OK!" `escalate.allOk` runs `RecordCareOutcome(allOk)` and goes back to the Neighbours list.
- "Yes, Alert emergency services" `escalate.alertServices` runs `EscalateAlert` and opens the Emergency sheet.

**Emergency sheet** (iOS action sheet / Android Material dialog)
- Title "Alert emergency services". Message "Do you want to call".
- "999" `emergency.call999` and "111" `emergency.call111` **do not dial**. Each closes the sheet and goes to Alive, which shows "Demo: would call 999" (or 111) in a banner `emergency.demoMessage` above the question.
- "Cancel" `emergency.cancel` closes the sheet.

**Is your neighbour alive?**
- Title "Emergency". "Emergency services alerted, Is your neighbour alive?".
- "No" `alive.no` runs `RecordCareOutcome(deceased)`.
- "Yes" `alive.yes` runs `RecordCareOutcome(alive)`.
- Both go to the Neighbours list.

### Hospital (5 steps: Alert = 1, In care = 2, Recovering = 3, Recovered / Bad condition = 4, Alive = 5)
**Patients list**
- Title "Patients". Rows `patients.row.<id>` (grouped list: avatar, name, score pill).
- "Simulate alert" `patients.simulateAlert` runs `SimulateAlert(hospital)`. It picks the lowest-scoring patient (Arthur Patel) and goes to Hospital alert.

**Hospital alert**
- Same screen as Help alert: title "Urgent alert", banner and countdown ring.
- "The patient Arthur Patel needs your help, please acknowledge the notification" `hospitalAlert.message`.
- "Accept" `hospitalAlert.accept` runs `AcknowledgeAlert` and goes to In care.
- Countdown `hospitalAlert.countdown`.

**Steps after the alert.** Titles: In care "In care", Recovering and Recovered "Recovery", Bad condition
"Condition", Patient alive "Patient status".

| Step | Heading | Answer | Test ID | Records | Goes to |
|---|---|---|---|---|---|
| In care | "Have you taken them in your care?" | "Yes" | `inCare.yes` | `inCare` | Recovering |
| Recovering | "Are they recovering?" | "Yes" | `recovering.yes` | `recovering` | Recovered |
| | | "No" | `recovering.no` | — | Bad condition |
| Recovered | "Have they recovered?" | "Yes" | `recovered.yes` | `recovered` | Patients list |
| Bad condition | "Are they in bad condition?" | "Yes" | `badCondition.yes` | `badCondition` | Patient alive |
| | | "No" | `badCondition.no` | — | Patients list |
| Patient alive | "Is your patient alive?" | "No" | `patientAlive.no` | `deceased` | Patients list |
| | | "Yes" | `patientAlive.yes` | `alive` | Patients list |

## Care outcomes → timeline copy
| Outcome | Copy | Colour | Resolves alert |
|---|---|---|---|
| `allOk` | "Neighbour reports all OK" | ok | yes |
| `inCare` | "In care in hospital" | warning | no |
| `recovering` | "Recovering" | ok | no |
| `recovered` | "Recovered" | ok | yes |
| `badCondition` | "In bad condition" | critical | no |
| `alive` | "Confirmed alive" | ok | yes |
| `deceased` | "Reported deceased" | critical | yes |

Other event copy: "Score N" (critical if <70, else ok), "Missed check-in" (critical),
"Notification sent to 1st level" / "Notification sent to 2nd level" (critical),
"1st level acknowledged" / "2nd level acknowledged" (warning), "Neighbour reached" (ok),
"Escalation to 2nd level" (critical).

## Open questions
1. **Scoring formula.** The formula above is a placeholder, and the thresholds and weights need clinical input.
2. **Death-notification consent.** Who may see "Reported deceased", and how is the family told?
3. **Acknowledgement deadline for a missed check-in.** The 24-hour threshold and the
   deadlines by score band are specified, but not the deadline for an alert with no score. This uses 15 min as a placeholder.
4. **"Not in bad condition" / "not yet recovered".** The prototype doesn't say what to record, so these go back to the list without an outcome.
