# PH-ResQ Vanguard 2026 — Work Rules

These rules govern planning and future implementation. The current repository is a documentation/planning baseline until actual Android source, builds, and test evidence are present.

## 1. Scope preservation and change control

- Preserve every requirement in `FEATURE_TRACEABILITY.md`. Do not silently delete, weaken, or replace a feature.
- If a requirement conflicts with Android platform behavior, security, licensing, or another requirement, document the conflict and a technically feasible alternative in `ARCHITECTURE_DECISIONS.md`; preserve the user-facing goal where possible.
- The repository owner has explicitly authorized the current documentation correction on branch `docs/architecture-correction-2026-10-27`. This authorization does not imply permission to merge directly to `main`, deploy, or release.
- For future destructive or scope-changing work, explain impact before acting. Use a branch, inspect diffs, and keep recoverable history.

## 2. Truthful status and verification

- Never describe a planned feature as implemented or tested without code/test evidence.
- Do not claim “zero bugs,” “zero crashes,” “100% guaranteed,” “absolute security,” “un-bypassable,” or “zero indication.” Use measurable criteria and record residual risk.
- Validate the latest revision, not an older successful build.
- Every completion claim must link to the relevant code, command/test output, commit SHA, and device/environment where applicable.
- A skipped or unexecuted test is not a pass.

## 3. Android-first architecture

- Use Android APIs for permissions, lifecycle, location, SMS, notifications, foreground services, BLE, Wi-Fi Direct, and Keystore.
- Browser-only APIs (`window`, `localStorage`, IndexedDB, DOM canvas) must not be presented as native Android implementations.
- Initial UI/build baseline is Kotlin + Jetpack Compose, minSdk 26, compileSdk/targetSdk 35, Java 17; unit tests and debug/release assemble passed on the baseline SHA recorded in IMPLEMENTATION_STATUS.md. Re-run CI on every candidate; database completeness, native services, data and supported-device matrix remain open.
- Follow Android permission, foreground-service, notification, and background-execution rules for the chosen target SDK.

## 4. Security and privacy

- Minimize collection, storage, broadcast, logging, and sharing of personal data.
- Encrypt sensitive vault data with authenticated encryption and a documented key lifecycle. Never reuse an AES-GCM nonce with the same key.
- Prefer Android Keystore-backed keys where feasible; document fallback, StrongBox availability, backup, reinstall, recovery, rotation, and PIN rate limiting.
- Never place secrets, precise location, medical details, or contact data in logs or unauthenticated BLE advertisements.
- A duress PIN is not guaranteed to be undetectable or to protect a user from physical harm. Do not promise absolute safety.
- A QR code is readable by any camera that can scan it; include only user-selected fields and explain disclosure.

## 5. SOS and data integrity

- Persist important SOS transitions before attempting external communication.
- Distinguish queued, sending, handed off/sent, delivered (only with confirmation), acknowledged, cancelled, and failed states.
- Use bounded retries, backoff, stable event IDs, and deduplication where supported. Do not guarantee carrier delivery.
- SHA-256 requires a trusted expected digest/manifest. A hash alone does not repair altered data.
- Persist critical state at transition boundaries; a low-battery callback below 5% is best effort, not the only save mechanism.

## 6. BLE/Wi-Fi Direct and spatial data

- Do not use placeholder permission checks, log-only scanning, or methods that always return success as implementations.
- Specify protocol version, authentication, message ID, TTL/hop limit, acknowledgements, retries, deduplication, replay protection, payload limits, and malformed-packet behavior.
- Treat RSSI distance as approximate and document uncertainty. Trilateration needs known reference points and suitable geometry.
- Benchmark the actual spatial query/index on realistic data and target devices; do not promise sub-10ms nationwide queries without evidence.

## 7. Data sources and compliance

- Verify authoritative source, license, attribution, retrieval date, version, update cadence, coverage, and package size before making nationwide claims.
- Clearly label missing, stale, or unavailable data.
- Cell Broadcast is controlled by Android/device/carrier/government systems. Do not claim that an ordinary app intercepts every broadcast.
- Only display verified helpline numbers and identify jurisdiction/source.

## 8. Testing and release

- Maintain `FEATURE_TRACEABILITY.md`, `TEST_ACCEPTANCE_MATRIX.md`, and `DATA_SOURCES_AND_RISKS.md`.
- Test denied permissions, no internet, no signal/SIM, stale GPS, process death, reboot, storage exhaustion, corrupt data, interrupted migrations, duplicate/out-of-order packets, and device incompatibilities.
- Release requires reproducible build evidence, current-revision test reports, physical-device validation for critical paths, source/license audits, and documented residual risks.
- Never merge or release solely because the documentation is polished.