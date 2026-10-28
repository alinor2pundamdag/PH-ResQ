# PH-ResQ Architecture Decisions

**Status:** Baseline architecture and partial implementation are recorded; CI passed on the stated baseline SHA, while feature and device validation remain pending.
**Last reviewed:** 2026-10-27

## ADR-001 — Native Android implementation baseline
**Decision:** Implement the first Android client in Kotlin with Jetpack Compose. The configuration uses minSdk 26, compileSdk 35, targetSdk 35, Java 17, Android Gradle Plugin 8.7.3, and Kotlin 2.0.21. The baseline passed unit-test, debug-assemble, and release-assemble tasks on commit `408e2aaaea09df70400c92038b405c5d5a47d337`; this does not certify device compatibility or feature completeness.
**Reason:** The app depends on Android location, messaging handoff, notifications, BLE, Wi-Fi Direct, Keystore, and lifecycle APIs.
**Consequences:** Browser APIs such as window, localStorage, IndexedDB, and DOM canvas are not implementation substitutes. Device-specific capabilities must use native Android APIs and permission/lifecycle handling.

## ADR-002 — Consent is enforced inside the app
Persist a versioned consent state and check it at launch and protected navigation. Declining blocks protected features and provides a clear exit path. An ordinary app cannot reliably force-stop itself or guarantee Android navigates to the home screen.

## ADR-003 — Durable SOS state and honest delivery status
Persist SOS transitions and distinguish queued, sending, transport handoff, confirmed delivery, acknowledgement, cancellation, and failure. A successful API/intent handoff is not recipient delivery. Current scaffold hands off to a messaging app and does not confirm delivery.

## ADR-004 — Security keys and vault lifecycle
Use authenticated encryption, with keys protected by Android Keystore where feasible. The current source includes an AES-GCM field cipher and a PBKDF2-derived AES-GCM PIN-protected vault with temporary lockout. This is partial protection, not a duress vault or a complete recovery/backup/key-rotation design. Before release, verify nonce uniqueness, key rotation, PIN derivation/rate limits, backup/reinstall behavior, fallback, and recovery. StrongBox is optional and device-dependent.

## ADR-005 — Mesh protocol primitives precede radio integration
A versioned envelope codec and durable encrypted outbox now provide packet bounds, HMAC validation, expiry/hop checks, retry state, ACK verification primitives, and replay suppression. They do not establish peer identity or shared keys and are not connected to BLE data exchange, Wi-Fi Direct, or a multi-hop forwarding service. Operational mesh remains unimplemented until authenticated peer exchange, radio transport, and multi-device acceptance pass. Do not advertise sensitive precise location or medical data.

## ADR-006 — RSSI is approximate
RSSI may support rough proximity only. Exact distance and trilateration are not guaranteed; show uncertainty or unavailable status unless reference geometry and measurements meet documented quality criteria.

## ADR-007 — Data coverage and maps require evidence
Nationwide coverage, administrative counts, licensing, package size, and update freshness must be verified against authoritative sources. Planning counts are not proof of completeness. No verified nationwide dataset or offline map package is currently included.

## ADR-008 — Integrity hashes require a trusted reference
SHA-256 mismatch detection must compare data against a trusted expected digest or authenticated manifest. A checksum alone does not repair data; recovery needs a verified backup or trusted source.

## ADR-009 — Release readiness is evidence-based
“Zero bugs,” “zero crash,” “100% guaranteed,” “un-bypassable,” and “zero indication” are not acceptable verified outcomes. Use reproducible tests, measured performance, documented limitations, and residual-risk records.

## ADR-010 — Initial SMS policy is explicit messaging-app handoff
The first scaffold uses ACTION_SENDTO so the user can review and send the SMS. It does not request SEND_SMS or silently dispatch. Multi-recipient URI behavior is device/messaging-app dependent and must be verified; if unreliable, implement a user-confirmed per-recipient workflow. This policy does not satisfy unattended dispatch requirements.

## ADR-011 — Local persistence is incremental and evidence-gated
The current source has SQLite schema version 7 with SOS, location, search/bookmark, directory, map metadata, advisories, evacuation centres, mesh queue/replay cache, family check-ins, and pending-action tables. A user-selected JSON importer handles directory/advisory/evacuation datasets; repository coverage and all migrations are not yet fully accepted. Consent and selected preferences use SharedPreferences; SOS draft/recipient/location fields use encrypted columns; the optional PIN vault and encrypted mesh outbox exist as partial components. This does not prove all six persistence layers, sync, retention, backup/recovery, or key lifecycle are production-complete. Migration fixtures, transactional invariants, key-loss behavior and physical-device recovery must pass before release.

## Open implementation decisions
- Complete SOS transition persistence and transport-backed delivery/acknowledgement evidence
- Authenticated peer-key establishment and real BLE/Wi-Fi Direct transport
- Current-head CI and physical-device acceptance for the latest source revision
- Consistency review across `SOS_STATE_MACHINE.md`, `DATABASE_SPEC.md`, `SECURITY_MODEL.md`, `MESH_PROTOCOL.md`, and the acceptance plan
- Complete repositories/migrations for the existing SQLite schema and a benchmarked spatial-index strategy
- Authoritative directory/map/weather sources, licenses, and update pipeline
- Location acquisition and stale-location/manual fallback behavior
- Voice-note recording, encryption, size limits, and retention
- BLE/Wi-Fi Direct supported device matrix and packet protocol
- Vault PIN/key recovery, backup/reinstall policy, and duress threat model
- Offline map packaging, versioning, integrity, and update rollback
- Release signing, privacy/legal review, and supported-device matrix
