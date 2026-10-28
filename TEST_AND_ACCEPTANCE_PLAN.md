# PH-ResQ Test and Acceptance Plan

**Status:** Normative plan; automated tests and CI cover a subset only. The latest current-head workflow and physical-device evidence must be checked independently.
**Related:** `TEST_ACCEPTANCE_MATRIX.md`, `FEATURE_TRACEABILITY.md`, `RELEASE_CANDIDATE_CHECKLIST.md`.

## 1. Acceptance rule

A feature is accepted only when all four are present: (1) requirement ID and intended behavior, (2) implementation reference, (3) passing reproducible test evidence from the candidate revision, and (4) residual risks/platform limitations. A source file, passing compile, mock, or old CI run alone does not satisfy acceptance.

Every result records commit SHA, test ID, build variant, OS/API, device model, setup, steps, expected and observed results, logs/screenshots where safe, tester/date, and issue link. Never include real emergency contact data, secrets, or precise personal location in evidence.

## 2. Test layers

1. Static checks and lint.
2. Unit tests for pure state, validation, cryptography, token, retry and geospatial utilities.
3. Robolectric/local database tests for migrations, persistence, import rollback and notification primitives.
4. Instrumented Android tests for UI, permission flows, process lifecycle, foreground service, and accessibility.
5. Physical-device integration tests for messaging-app handoff, SMS/carrier differences, GPS, BLE, Wi-Fi Direct, notification behavior, audio and background restrictions.
6. Data-quality and licensing checks for official sources, coverage, update freshness and offline package size.
7. Resilience/security tests and controlled user acceptance.

## 3. P0 test groups

### Consent and privacy
- First launch and consent acceptance/decline.
- Relaunch and process death preserve consent version without bypassing protected screens.
- Permission denial/revocation and data deletion.
- Lock-screen notification redaction and log review.

### SOS and messaging
- Press less than two seconds, exactly two seconds, release/cancel, repeated taps and accessibility alternative.
- Durable event write before handoff, process death before/after handoff, no SIM, no compatible messaging app, denied permission, offline network, stale/missing location.
- Ensure handoff never renders as delivered; receipt and ACK must be authenticated and event-bound.
- Voice note size, encryption, playback, share chooser, cancellation, storage exhaustion and retention.

### Storage, cryptography and migration
- Upgrade every supported DB version through schema v7, interrupted migration, corrupt row and low-storage failure.
- AES-GCM tamper rejection, unique nonce generation, key unavailable/reinstall behavior, PIN lockout and replacement.
- Ensure sensitive data is absent from logs, unencrypted backups and lock-screen notifications.
- Import valid/malformed/oversized/duplicate/stale JSON and prove failed imports preserve prior dataset.

### Mesh
- Packet version, malformed length, oversize, bad HMAC, replay, expiry, clock skew, hop boundaries, queue retry exhaustion and ACK mismatch.
- Mark radio delivery tests as NOT IMPLEMENTED until an actual BLE/Wi-Fi transport and authenticated peer-key establishment exist.

### Accessibility and UI/UX
- TalkBack labels/order, font scaling, high contrast, reduce motion, touch targets, haptics and non-color-only status.
- Confirm decorative animation never delays or overlays SOS controls.
- Check portrait/landscape and supported screen sizes.

### Data, maps and offline
- Airplane mode with previously imported data; source/license/version metadata; stale labels; duplicate/invalid coordinates; nationwide coverage report.
- Verify licensed map renderer/package and offline rendering before claiming offline maps are complete.
- Confirm cell-broadcast copy does not imply the app intercepts system broadcasts.

## 4. P1 tests

- Cold start, search latency on realistic dataset, memory, battery use, APK size, offline data package size.
- Family check-in local/remote semantics, queue idempotency and recovery.
- Clock changes, timezone changes, reboot, task removal, app upgrade and storage pressure.
- Multiple Android versions and device manufacturers for permissions, BLE, notification and foreground-service behavior.

## 5. Stop-ship conditions

Any P0 failure; data loss of a pending SOS; duplicate dispatch caused by recovery; false delivered/acknowledged UI; plaintext sensitive values in logs/backups; unauthenticated mesh ACK; invalid or unlicensed bundled data; unrecoverable schema migration; or an APK whose source SHA does not match the intended commit.

## 6. Evidence and release gate

Use the current `TEST_ACCEPTANCE_MATRIX.md` as the execution ledger. A successful CI run must match the intended PR head SHA and contain the APK, `BUILD_METADATA.txt`, and `SHA256SUMS`. Physical-device and data-source acceptance are separate release gates; unsigned debug/release APKs are for controlled testing only and are not a production release.
