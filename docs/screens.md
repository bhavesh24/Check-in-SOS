# Check-in SOS — screen inventory (single source of truth)

Both apps (iOS + Android) mirror this file exactly: copy, flow, test IDs and mock data.
If a screen changes, change it here first, then in both apps.

**Out of scope (login/onboarding):** enrollment "Do you have a code?" (older adult, neighbour), hospital
empanelment code, family registration wizard + "All Done!" confirmation. The role picker is the demo entry point.

## Global rules
- Every screen except Home has a back control (iOS nav-bar back, Android top-app-bar up + system back). Test ID `nav.back`.
- Platform-only test IDs: `screen.title` is the Android top-app-bar title (iOS tests read the navigation bar), and `keyboard.done` is the iOS keyboard toolbar button (Android uses the IME action).
- Multi-step flows show a step indicator "Step N of M" (test ID `stepProgress`). Step counts are listed per flow below.
- Theme: teal, **darkened** so white-on-teal passes WCAG AA (≥4.5:1). Touch targets ≥ 48×48 pt/dp; primary buttons ≥ 56 high, full width.
- Text scales with Dynamic Type (iOS) / font scale (Android); no fixed-height text containers.
- Push notifications are **simulated** by a "Simulate alert" button. 999/111 **never dials** in this build.
- Any unexpected failure loading or saving shows "Something went wrong. Please try again." (test ID `error.message`) on the current screen.
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
| Score numeral | 72 pt/sp, primary | | rendered as `76/` newline `100` like the prototype |

## Fixed values for tests
- Fixed clock ("now"): **2026-01-15T09:00:00Z**.
- Current older adult (older-adult role): `oa-1`.

## Mock data (seeded by `MockDataSource`)
Older adults (family "loved ones" and neighbour "neighbours" lists both show these three, in this order):

| id | Name | Latest score | 1st-level neighbour (first / middle / last / email / mobile) |
|---|---|---|---|
| `oa-1` | Margaret Wilson | 76 | Alex / — / Brown / alex.brown@example.com / 07123456789 |
| `oa-2` | Arthur Patel | 38 | Priya / — / Shah / priya.shah@example.com / 07234567890 |
| `oa-3` | Edith Clarke | — (none yet) | Tom / J / Hughes / tom.hughes@example.com / 07345678901 |

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
- Missed check-in (no score): 30 min. This is a placeholder; see open questions.

The alert countdown counts down to `raisedAt + timeout`, shown as `mm:ss`.

Escalation levels: **1** = neighbour, **2** = emergency services / hospital.

## Screens

### Home: role picker
- Title: "Home". Heading: "Are you".
- Buttons, in this order:

| Label | Test ID | Goes to |
|---|---|---|
| "Old age adult" | `home.role.olderAdult` | Assessment |
| "Concerned family" | `home.role.family` | Loved ones |
| "Hospital" | `home.role.hospital` | Patients |
| "Neighbour" | `home.role.neighbour` | Neighbours |

### Older adult (2 steps: Assessment = 1, Score = 2)
**Assessment**
- Title "Assessment".
- Section "Are you" (physical), choose one:
  - "Fine" `assessment.physical.fine`
  - "Good" `assessment.physical.good`
  - "Ill" `assessment.physical.ill`
  - "Very sick" `assessment.physical.verySick`
- Section "Are you" (mood), choose one:
  - "Happy" `assessment.mood.happy`
  - "Pleasant" `assessment.mood.pleasant`
  - "Anxious" `assessment.mood.anxious`
  - "Depressed" `assessment.mood.depressed`
- "Submit" `assessment.submit` is disabled until both sections are answered. It runs `SubmitAssessment` and goes to Score.
- Demo link "Demo: missed check-in" `assessment.demoNoCheckIn` goes to No check-in.

**Score**
- Title "Score".
- Numeral `score.value`, shown as "76/100" (accessibility label "Score 76 out of 100").
- If the score is < 70: "A notification has been sent to your neighbour" `score.neighbourNotified`.
- "Done" `score.done` returns to Home.

**No check-in** (registered but not checked in after the threshold time)
- Title "No Check-in".
- Message "As you have not checked in, a notification has been sent to your neighbour" `noCheckIn.message`.
- Back only.

### Concerned family
**Loved ones**
- Title "Your loved ones".
- Rows show the name and the latest score ("Score 76" / "No score yet").
- Test ID `lovedOnes.row.<id>`. A row opens Current status.

**Current status**
- Title = the person's name.
- Big score numeral `status.score` (or "—"), with caption "Current score".
- "1st level: Alex Brown" `status.neighbour`, with a pencil button `status.editNeighbour` (label "Edit neighbour") that goes to Edit neighbour.
- "Timeline" `status.timeline` goes to Timeline.

**Edit neighbour**
- Title "Edit Neighbour". Heading "Your 1st level notified person".
- Fields, pre-filled:
  - First name* `editNeighbour.firstName`
  - Middle name `editNeighbour.middleName`
  - Last name* `editNeighbour.lastName`
  - Email* `editNeighbour.email`
  - Mobile* `editNeighbour.mobile`, after a fixed "+44" prefix. The field shows the stored number without its leading "0" ("07123456789" → "7123456789"); on save a "0" is put back unless the text already starts with "0" or "+".
- A "Done" button above the keyboard `keyboard.done` closes it (the phone pad has no return key).
- "Save" `editNeighbour.save` runs `UpdateNeighbour`, then goes back to Current status, which shows the new name.
- Validation errors appear in `editNeighbour.error`:
  - "First name is required"
  - "Last name is required"
  - "Enter a valid email"
  - "Enter a valid UK mobile number" (after removing spaces/dashes and a leading "+44" or "0", 9–10 digits must remain)

**Timeline**
- Title "Timeline".
- A vertical list of events, oldest first. Each row has a coloured ring, the copy and the time (HH:mm).
- Test ID `timeline.event.<index>`. When there are no events: "No events yet" `timeline.empty`.

### Neighbour (4 steps: Alert = 1, Reached = 2, Escalate = 3, Alive = 4)
**Neighbours list**
- Title "Neighbours". Rows `neighbours.row.<id>` are informational.
- "Simulate alert" `neighbours.simulateAlert` runs `SimulateAlert(neighbour)`. It picks the lowest-scoring older adult (Arthur Patel, 38, so the timeout is 20 min) and goes to Help alert.

**Help alert**
- Title "Help".
- "Your neighbour Arthur Patel needs your help, please acknowledge the notification" `alert.message`.
- "Accept" `alert.accept` runs `AcknowledgeAlert` and goes to Reached.
- Countdown `alert.countdown`, e.g. "20:00", ticking each second. Display only; on expiry it shows "Time expired".

**Have you reached?**
- Title "Help".
- "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached" `reached.message`.
- "I have reached" `reached.confirm` runs `MarkArrived` and goes to Escalate.

**Escalate**
- Title "Help". Heading "Do you want to escalate?".
- "No, All OK!" `escalate.allOk` runs `RecordCareOutcome(allOk)` and goes back to the Neighbours list.
- "Yes, Alert emergency services" `escalate.alertServices` runs `EscalateAlert` and opens the Emergency sheet.

**Emergency sheet** (iOS action sheet / Android Material dialog)
- Title "Alert emergency services". Message "Do you want to call".
- "999" `emergency.call999` and "111" `emergency.call111` **do not dial**. Each closes the sheet and goes to Alive, which shows "Demo: would call 999" (or 111) in a banner `emergency.demoMessage` above the question.
- "Cancel" `emergency.cancel` closes the sheet.

**Is your neighbour alive?**
- Title "Help". "Emergency services alerted, Is your neighbour alive?".
- "No" `alive.no` runs `RecordCareOutcome(deceased)`.
- "Yes" `alive.yes` runs `RecordCareOutcome(alive)`.
- Both go to the Neighbours list.

### Hospital (5 steps: Alert = 1, In care = 2, Recovering = 3, Recovered / Bad condition = 4, Alive = 5)
**Patients list**
- Title "Patients". Rows `patients.row.<id>`.
- "Simulate alert" `patients.simulateAlert` runs `SimulateAlert(hospital)`. It picks the lowest-scoring patient (Arthur Patel) and goes to Hospital alert.

**Hospital alert**
- Title "Help".
- "The patient Arthur Patel needs your help, please acknowledge the notification" `hospitalAlert.message`.
- "Accept" `hospitalAlert.accept` runs `AcknowledgeAlert` and goes to In care.
- Countdown `hospitalAlert.countdown`.

**Steps after the alert** (title "Help" on every screen)

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
2. **Neighbour fallback.** What happens when the countdown expires without acknowledgement? The prototype doesn't say. Here the countdown is display-only.
3. **Death-notification consent.** Who may see "Reported deceased", and how is the family told?
4. **Missed check-in timeout.** This uses 30 min as a placeholder.
5. **"Not in bad condition" / "not yet recovered".** The prototype doesn't say what to record, so these go back to the list without an outcome.
