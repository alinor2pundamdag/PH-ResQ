# PH-ResQ Vanguard 2026 — Consolidated Feature Inventory

**Status:** This is a requirements inventory, not proof that features have been implemented. All items remain in scope unless a documented decision is reviewed and approved. Detailed test links live in `FEATURE_TRACEABILITY.md`.

## A. Offline-first and nationwide information

- Offline-first emergency workflows and local caching.
- Nationwide emergency directory intended to include regions, provinces, cities, municipalities, and barangays.
- Offline maps/map tiles with source/license, coverage, version, freshness, and package-size metadata.
- Search and bookmark history.
- Verified emergency helpline fallback.
- Evacuation-centre information/status.
- PAGASA advisory cache with source, publication/retrieval time, and stale/expiry handling.
- Cell-broadcast awareness that does not falsely claim third-party interception of all alerts.
- Dataset update, rollback, validation, missing-data behavior, and licensing/attribution controls.

## B. SOS and communications

- Two-second press-and-hold SOS activation with haptic feedback and anti-accidental guard.
- Tagalog emergency SMS workflow to up to 10 configured emergency contacts, including location, timestamp, and accuracy when available.
- Voice/audio quick note, intended as a short encrypted SOS attachment (planned target: 10 seconds), with permission, size, and storage limits.
- Explicit states for queued, sending, transport handoff, delivered only when confirmed, acknowledged, cancelled, retry pending, and failed.
- Family “Ligtas Ako” check-in and durable offline sync queue.
- Location fallback with stale timestamp/accuracy labels and manual location option.
- Stealth audio mute/silent dispatch option, while preserving visual status and cancellation.

## C. Persistence and recovery

Preserve all six persistence areas:
1. Active mode.
2. Encrypted emergency-contact vault.
3. Last known GPS fallback.
4. Crisis SOS state and queue.
5. Search/bookmark history.
6. Offline map tiles/cache.

Also preserve:
- Best-effort low-battery snapshot below 5%.
- Continuous persistence at critical state transitions.
- SHA-256 integrity check against a trusted expected digest/manifest.
- Verified backup/re-download recovery rather than claiming checksum-only self-healing.
- Atomic transactions, migration handling, and recovery from process death, reboot, interrupted writes, and low storage.

## D. Mesh, proximity, and spatial lookup

- BLE peer discovery and proximity hints.
- Wi-Fi Direct peer connection and payload transfer.
- Offline mesh forwarding/multi-hop behavior.
- Fifteen-second battery-saver scan window as a configurable policy subject to Android scheduling limits.
- Geohash/local spatial grouping and a measured query/index strategy.
- Rolling privacy tokens and peer authentication.
- Versioned packets with IDs, TTL/hop limit, acknowledgement, bounded retries/backoff, duplicate suppression, replay resistance, payload/rate limits, and malformed-packet rejection.
- RSSI distance is approximate; no exact-distance guarantee. Trilateration is conditional on suitable known references and geometry.

## E. Security and medical information

- PIN-protected encrypted vault using authenticated encryption and Android Keystore where feasible.
- PIN derivation/rate limiting, key lifecycle, nonce uniqueness, rotation, backup/reinstall/recovery behavior.
- Optional duress PIN and dummy vault with explicit threat-model limits; no “zero indication” or absolute physical-safety promise.
- Offline Medical ID QR with user-selected minimal fields and an explicit disclosure warning.
- Rolling tokens and privacy controls.
- No secrets or sensitive personal data in logs or unauthenticated broadcasts.

## F. UX, accessibility, and Android safeguards

- Philippine-flag color-looping title and waving flag visual identity.
- Clean emergency-first UI; decorative animation never obscures or delays emergency controls.
- High-contrast mode, scalable text, TalkBack labels, large touch targets, reduced motion, and haptic controls.
- Android foreground service with required visible notification/actions where appropriate.
- Safe volume policy and stealth mode.
- Runtime permissions with plain-language rationale and safe denial paths.
- Lifecycle handling for process death, task removal, reboot, app upgrade, permission revocation, and Android background restrictions.

## G. Verification and quality

- Concurrency, atomic persistence, duplicate/out-of-order message, and interrupted-operation tests.
- Chaos tests: no internet, no signal/SIM, no GPS fix, denied permissions, Bluetooth/Wi-Fi disabled, process kill, reboot, low storage, corrupt database, interrupted migration, battery below 5%, clock changes.
- Security tests: nonce uniqueness, vault lockout, replay, tampering, backup leakage, log redaction, and duress threat-model review.
- Performance tests on realistic nationwide data and low-end supported hardware.
- Build, source, data, license, physical-device, and release evidence.

## Status rule

Each requirement is **Planned**, **Implemented**, **Tested**, **Blocked**, or **Accepted with limitation** according to evidence. Never mark all features complete merely because they are listed here.