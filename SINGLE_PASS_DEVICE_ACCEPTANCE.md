# PH-ResQ — Single-Pass Device Acceptance Runbook

**Purpose:** One consolidated physical-device test session and one report covering all 34 requirements. This runbook is not evidence that any test has passed. Do not start the final session until the implementation gate below is met.

## Gate before the one physical-device test session

- [ ] All 34 rows in `FEATURE_TRACEABILITY.md` are implemented or have an explicitly approved, testable limitation; no row is silently treated as complete.
- [ ] All CI checks pass on the exact candidate commit: lint, unit/Robolectric tests, instrumentation tests that are configured, debug/release assembly, artifact upload, and SHA-256 evidence.
- [ ] No known P0 failure remains in consent, SOS preparation, encryption/privacy, persistence, or fallback messaging.
- [ ] Candidate APK, commit SHA, `BUILD_METADATA.txt`, and `SHA256SUMS` are saved together.
- [ ] Test dataset files are source-checked, license-checked, date-stamped, and included in the report. A JSON field saying "verified" is not cryptographic proof.
- [ ] A non-emergency recipient/test number is available. Never test by placing a real emergency call or sending a false SOS to a public emergency service.

## Minimum test setup

1. **Primary Android phone:** target candidate device, fresh install and upgrade path where relevant.
2. **Second Android phone:** required for peer discovery, Wi-Fi Direct connection/transfer, mesh packet/ACK/replay scenarios, and interoperability.
3. **SIM/network states:** available SIM and mobile data; then airplane mode/offline; then denied/no-SIM cases where applicable.
4. **Radios and permissions:** Bluetooth, Wi-Fi/Wi-Fi Direct, location, microphone, and notifications can be independently enabled/disabled and denied.
5. **Data fixtures:** known-good directory/advisory/evacuation JSON, malformed JSON, oversized JSON, stale records, and a deliberately modified file for integrity checks.
6. **Evidence capture:** screenshots/screen recordings with personal information redacted, test timestamps, app/device/API versions, relevant logs, and battery readings.

If a feature inherently requires an extra environment (for example, a supported Wi-Fi Direct device pair, SIM/carrier delivery, or a licensed offline map dataset), mark that case **BLOCKED — setup unavailable**, not PASS.

## One-session execution order

Run all applicable cases in `TEST_ACCEPTANCE_MATRIX.md` during the same planned session, grouped as follows:

1. Install, consent, permissions, privacy controls, and clean/upgrade launch.
2. SOS hold, accessible alternative, cancellation, contact selection, location freshness, message handoff, voice-note sharing, and visible foreground status.
3. Encryption/vault lockout, Medical ID QR disclosure, volume/reduce-motion/high-contrast, notification denial, and permission revocation.
4. Offline directory/search/nearby search, advisories, evacuation records, stale-data handling, map cache/rendering, import validation, and integrity/tamper checks.
5. Family check-in, persistence across activity/process restart and reboot, migration/recovery/corruption/low-storage scenarios.
6. BLE discovery, Wi-Fi Direct pairing/transfer, authenticated packet handling, queue/retry/ACK/replay, duplicate/out-of-order messages, and multi-hop behavior where implemented.
7. Network/SIM/GPS/Bluetooth/Wi-Fi/notification denial, battery saver, low battery, process death, reboot, and recovery.
8. Accessibility: TalkBack, large text, contrast, touch targets, reduce-motion, and emergency-control reachability.
9. Re-run all failed cases after fixes; do not replace a failed result with a later pass without retaining both attempts and the fix commit.

## Required single report format

Copy this template once after the session. Include one result per test ID; attach evidence for every FAIL/BLOCKED and every P0/P1 PASS.

```text
PH-ResQ CONSOLIDATED DEVICE ACCEPTANCE REPORT
Candidate commit SHA:
APK filename and SHA-256:
CI run URL and result:
Test date/time and tester:
Primary device / Android API / build:
Secondary device / Android API / build:
SIM/carrier/network conditions:
Offline dataset versions, source URLs, licenses, and dates:

Summary: PASS __ / FAIL __ / BLOCKED __ / NOT RUN __
P0 gate: PASS / FAIL
P1 gate: PASS / FAIL
Overall: ACCEPT / REJECT / INCOMPLETE

For each T-xxx:
ID | PASS/FAIL/BLOCKED/NOT RUN | device/setup | steps | observed result | expected result | evidence link/file | issue/commit
...
Defects grouped by severity:
P0:
P1:
P2:
P3:
Retest results and fixing commit SHAs:
Known limitations and unavailable test setups:
```

## Status semantics

- **PASS:** the expected behavior was observed on the specified physical setup and evidence is attached.
- **FAIL:** behavior differs from expectation, data loss occurs, privacy/security is violated, or the UI overstates delivery/coverage.
- **BLOCKED:** required device, data, carrier, license, or environment was unavailable. This is not a pass.
- **NOT RUN:** the case was not executed.
- **INCOMPLETE:** any required P0 case is failed, blocked, or not run; any critical feature has no credible evidence; or the APK/commit cannot be matched.

## Critical boundaries

A single consolidated report can cover all features, but the physical test setup may use two or more devices and multiple radio/network states. One phone cannot prove peer-to-peer transport or multi-hop behavior. A green CI run proves only the checks encoded in that workflow; it does not prove carrier delivery, nationwide data completeness, map licensing, radio interoperability, accessibility on every device, or emergency-service readiness.
