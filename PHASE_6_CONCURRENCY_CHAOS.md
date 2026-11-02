# Phase 6 — Concurrency, Lifecycle Recovery & Chaos Testing

**Status:** Partial automated unit/Robolectric tests exist, but the full chaos matrix, physical-device lifecycle tests, and recovery acceptance have not been run. No zero-crash or zero-bug guarantee is claimed.

## Objective
Prove that critical state remains consistent during concurrent work, interrupted operations, lifecycle changes, and resource failures on supported Android devices.

## 1. Concurrency and atomicity
- Use database transactions and a documented synchronization strategy for shared state.
- Define ownership/cancellation of long-running work and release BLE, Wi-Fi, location, audio, and database resources on lifecycle transitions.
- Avoid holding locks while performing network/radio operations.
- Make SOS enqueue/send and sync-queue processing idempotent where possible.
- Test concurrent SOS taps, duplicate callbacks, concurrent sync, repeated cancellation, and app background/foreground transitions.

## 2. Persistence and recovery
Exercise process death during consent persistence, SOS enqueue/send, vault update, map update, and family check-in. Verify recovery after reboot, app upgrade, permission revocation, and interrupted database migration.

Critical state must be persisted at each state transition. The below-5% battery snapshot is only an additional best-effort safeguard because Android may kill the process before the callback.

## 3. Required chaos scenarios
- No internet, no mobile signal, no SIM, no GPS fix, stale GPS.
- Location/SMS/microphone/notification/Bluetooth/nearby-device permissions denied or revoked.
- Bluetooth/Wi-Fi disabled; BLE peer disappears; Wi-Fi Direct handshake or transfer is interrupted.
- Duplicate, out-of-order, replayed, expired, malformed, and oversized mesh packets.
- Process kill, task removal, reboot, clock change, app upgrade, low memory, and background restriction.
- Storage exhaustion, corrupt database, interrupted write, partial download, invalid checksum/manifest, and failed recovery source.
- Battery drops below 5%, haptic/audio unavailable, voice-note permission denied, and notification action used.
- Nationwide data includes duplicates, missing coordinates, invalid hierarchy, stale alerts, and unavailable map regions.

## 4. Evidence and reporting
For each test, record:
- current commit SHA and build variant;
- device model, Android version/API, permissions, network/radio state;
- preconditions, exact steps, expected and observed result;
- logs/screenshots with personal data redacted;
- pass/fail/skipped status and linked issue for failures;
- measured startup/search latency, memory, battery, APK and offline-data size where applicable.

Do not label a test suite “complete” if scenarios were skipped. Do not claim zero crashes or zero bugs; report observed failures and residual risk.

## Current implementation boundary
Current automated tests cover selected core utilities, database behavior, dataset import, vault behavior, mesh queue primitives, and a foreground-notification primitive. This is not evidence that every scenario below has executed. Record each run against the exact candidate SHA in `TEST_ACCEPTANCE_MATRIX.md` and `TEST_AND_ACCEPTANCE_PLAN.md`.

## Acceptance criteria
- No critical SOS/consent/vault state is silently lost in tested failure scenarios.
- Duplicate callbacks and packets do not cause uncontrolled duplicate actions.
- Failed storage/network/radio operations produce recoverable states and accurate status.
- The release gate in `MASTER_PLAN.md` is met with evidence for the current revision.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-005 through T-027.
