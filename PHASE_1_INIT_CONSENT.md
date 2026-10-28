# Phase 1 — Android Foundation, Initialization & Consent

**Status:** Partial implementation exists for versioned consent/privacy notice and permission guidance; full navigation/lifecycle acceptance and physical-device validation remain pending. See `ARCHITECTURE_DECISIONS.md` and `FEATURE_TRACEABILITY.md`.

## Objective
Establish the Android project baseline, selected framework/SDK, privacy policy, persistent consent state, permission strategy, and a reproducible initialization flow.

## 1. Architecture and project setup
Initial baseline is recorded: Kotlin + Jetpack Compose, application ID ph.resq.app, minSdk 26, compileSdk/targetSdk 35, Java 17, AGP 8.7.3, Kotlin 2.0.21. The CI build must validate this baseline. Local database, supported-device/API matrix, and remaining native service modules still need implementation/verification. Use Android APIs for lifecycle, permissions, location, messaging handoff, notifications, BLE, Wi-Fi Direct, and Keystore. Browser-only APIs in earlier drafts are not native implementations.

Suggested module boundaries (names are illustrative and may be adapted to the selected framework):
- `app`: Android entry point, navigation, manifest, and application lifecycle.
- `core`: state models, error handling, configuration, and dependency wiring.
- `consent`: policy display, versioned consent record, and navigation guard.
- `storage`: durable local state and schema migrations.
- `security`: Keystore integration and integrity checks.
- `location`, `sos`, `mesh`, `directory`: isolated feature modules with explicit interfaces.

## 2. Startup and integrity check
1. Show a short, accessible startup screen; avoid blocking launch indefinitely on optional network work.
2. Load the local database and versioned state safely.
3. Verify packaged data against a trusted expected digest or authenticated manifest.
4. If integrity verification fails, mark the dataset unavailable, preserve recoverable user data, and use only a verified backup/re-download source. SHA-256 alone does not self-heal data.
5. Record startup failures without writing secrets or precise personal data to logs.

## 3. Persistent consent state
Required states: `NOT_DECIDED`, `ACCEPTED(policyVersion, timestamp)`, and `DECLINED(policyVersion, timestamp)`.

- On first launch, display the privacy notice, data-use explanation, and applicable license information before protected features.
- Accept: persist the policy version and timestamp, then unlock protected navigation.
- Decline: persist the declined state, explain that protected features remain unavailable, and block protected routes.
- On every launch and protected navigation, check consent before showing protected features.
- When policy/data-use changes materially, determine whether re-consent is required and document the policy-version rule.
- Let the user read the policy without forced exit or abrupt app closure.

**Android limitation:** An ordinary application cannot reliably force-stop itself or guarantee a transition to the home screen. Do not implement or claim a “persistent force-stop” feature. Consent is enforced within the app.

## 4. Permissions and privacy
Request a permission only when the associated feature is used and explain why it is needed. Define a safe denial path for location, SMS, microphone, notifications, Bluetooth/nearby devices, and Wi-Fi-related capabilities. Never bypass Android permission controls.

## Current implementation boundary
The source includes a consent store and privacy notice. This does not prove every protected route checks consent after process death, policy-version change, deep link, or restored task. Validate those paths against `TEST_AND_ACCEPTANCE_PLAN.md`.

## Acceptance criteria
- Consent cannot be bypassed through app navigation or relaunch.
- Decline does not falsely claim OS force-stop; protected features remain locked.
- Consent persists across process death and app restart.
- Integrity mismatch is detected against trusted metadata and does not trigger unverified repair.
- Permission denial does not crash or trap the user.
- Build and tests are run on the current revision and named Android environment.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-001 through T-003, T-011, T-025.
