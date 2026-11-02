# PH-ResQ Architecture Collection

**Collection version:** 1.1  
**Project:** PH-ResQ Vanguard 2026  
**Platform baseline:** Native Android — Kotlin, Jetpack Compose, Android lifecycle APIs  
**Status:** Consolidated architecture reference for a partially implemented project. This collection is not a declaration of feature completion, emergency-service certification, nationwide data coverage, or release readiness.  
**Updated:** 2026-11-02

## Purpose

This index joins the PH-ResQ architecture documents into one navigable source of truth. It explains how the Android client, local data, SOS workflow, privacy/security controls, offline datasets, and future peer-to-peer transports fit together. Existing detailed documents remain authoritative for their own domains.

## Canonical application identity and naming (required)

- **Exact public application name at every lifecycle stage:** `PH-ResQ`.
- Use `PH-ResQ` as the app/store listing title before download, the downloaded APK's visible filename prefix, the Android package-installer application label, and the installed launcher/application label. Keep the spelling, capitalization, and hyphen identical; do not rename it to a different title between stages.
- **Brand descriptor only:** `Vanguard 2026` may appear as a campaign/tagline or collection descriptor, but it is not part of the application name and must not replace `PH-ResQ` in the store title, APK name, installer label, or launcher label.
- **Android package identity:** `ph.resq.app` remains the release `applicationId`; the debug build may use its existing `.debug` suffix for side-by-side testing. Package IDs are technical identifiers, not the displayed application name.
- **Artifact filenames:** debug `PH-ResQ-debug.apk`; unsigned release candidate `PH-ResQ-release-unsigned.apk`. A future signed production artifact must use `PH-ResQ.apk` only after signing and release authorization.
- **Single source of truth:** Android's `@string/app_name` is `PH-ResQ`; store metadata, download page, QR/link landing page, release notes, and support materials must copy this exact canonical name. Any branding change requires an explicit user-approved naming decision and a coordinated update.
- **Acceptance check:** inspect the generated APK filenames and manifest application label for both debug and release variants; install each on Android and verify the package installer and launcher show exactly `PH-ResQ`.

## Architecture at a glance

1. **Presentation:** Jetpack Compose screens, accessible emergency actions, explicit consent, status/error/stale-data states.
2. **Application orchestration:** SOS state transitions, permission/lifecycle coordination, import workflows, user-confirmed external handoffs.
3. **Domain:** typed models and rules for SOS events, directory/advisory/evacuation records, mesh envelopes, and validation.
4. **Persistence:** SQLite local database, encrypted sensitive fields, dataset caches, pending-action and mesh outboxes.
5. **Platform adapters:** Android location, notification/foreground service, document picker, audio recording/playback, BLE discovery, Keystore, and external messaging/dialer intents.
6. **Optional connectivity:** BLE and Wi-Fi Direct are intended transport adapters. Current packet/queue primitives and BLE discovery do not constitute operational mesh delivery.
7. **External systems:** Android messaging/dialer apps, official data publishers, and future authenticated peer devices. PH-ResQ does not control recipient devices, carrier delivery, public data publishers, or emergency-service response.

See [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) for component boundaries, data flows, and Mermaid diagrams.

## Document map

### Architecture and decisions
- [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) — end-to-end component model, boundaries, SOS/data/mesh flows, failure semantics.
- [ARCHITECTURE_DECISIONS.md](ARCHITECTURE_DECISIONS.md) — accepted platform and behavioral decisions (ADRs).
- [MASTER_PLAN.md](MASTER_PLAN.md) — project goals, requirements, visual identity, and target behavior.
- [WORK_RULES.md](WORK_RULES.md) — change control, feature preservation, verification, and evidence rules.
- [IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md) — ordered gates and exit evidence.

### Domain specifications
- [SOS_STATE_MACHINE.md](SOS_STATE_MACHINE.md) — durable SOS lifecycle and truthful delivery states.
- [DATABASE_SPEC.md](DATABASE_SPEC.md) — local schema, persistence invariants, and import contract.
- [SECURITY_MODEL.md](SECURITY_MODEL.md) — assets, adversaries, trust boundaries, controls, and open risks.
- [MESH_PROTOCOL.md](MESH_PROTOCOL.md) — packet format, authentication requirements, retry, replay, and radio boundaries.
- [DATA_SOURCES_AND_RISKS.md](DATA_SOURCES_AND_RISKS.md) — source provenance, licensing, coverage, freshness, and data risk.

### Platform and feature phases
- [PHASE_1_INIT_CONSENT.md](PHASE_1_INIT_CONSENT.md) — Android foundation and consent.
- [PHASE_2_UI_UX_CANVAS.md](PHASE_2_UI_UX_CANVAS.md) — emergency-first UI, visual identity, and accessibility.
- [PHASE_3_DATABASE_GEOHASH.md](PHASE_3_DATABASE_GEOHASH.md) — local persistence and geospatial search.
- [PHASE_4_BLE_WIFI_MESH.md](PHASE_4_BLE_WIFI_MESH.md) — BLE, Wi-Fi Direct, and offline mesh.
- [PHASE_5_CRYPTO_VAULT.md](PHASE_5_CRYPTO_VAULT.md) — encryption, PIN vault, and privacy.
- [PHASE_6_CONCURRENCY_CHAOS.md](PHASE_6_CONCURRENCY_CHAOS.md) — lifecycle, concurrency, and resilience.

### Verification and release governance
- [FEATURE_TRACEABILITY.md](FEATURE_TRACEABILITY.md) — all 34 retained feature IDs and their evidence status.
- [TEST_ACCEPTANCE_MATRIX.md](TEST_ACCEPTANCE_MATRIX.md) — scenario-by-scenario test requirements.
- [TEST_AND_ACCEPTANCE_PLAN.md](TEST_AND_ACCEPTANCE_PLAN.md) — acceptance rules and test layers.
- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) — current implementation evidence and limitations.
- [RELEASE_CANDIDATE_CHECKLIST.md](RELEASE_CANDIDATE_CHECKLIST.md) — candidate validation and release gates.
- [OFFLINE_DATA_IMPORT.md](OFFLINE_DATA_IMPORT.md) — supported JSON import formats and limitations.
- [PRIVACY_NOTICE.md](PRIVACY_NOTICE.md) — user-facing data/permission notice.
- [SINGLE_PASS_DEVICE_ACCEPTANCE.md](SINGLE_PASS_DEVICE_ACCEPTANCE.md) — physical-device acceptance runbook.
- [PATCH_NOTES.md](PATCH_NOTES.md) — change history.
- [features_update.md](features_update.md) — retained feature inventory (must remain consistent with traceability).

## Source-of-truth hierarchy

When documents disagree, resolve in this order:

1. **Current source code and database migrations** define what the current build actually does.
2. **Architecture decisions** define accepted platform constraints and intentional behavior.
3. **Domain specifications** define required contracts and invariants.
4. **Feature traceability and implementation status** define what is implemented, tested, device-verified, or still open.
5. **Master plan and phase documents** define intended product scope; they do not override verified platform constraints or evidence.
6. **Release checklist and CI artifacts** determine whether a specific commit is build-validated.

A successful compile does not prove radio interoperability, SMS delivery, nationwide data coverage, accessibility, privacy compliance, or real-world rescue outcomes.

## Non-negotiable integration contracts

- **SOS:** persist an event before external handoff; distinguish queued, attempted, handed-off, transport-confirmed, recipient-acknowledged, cancelled, and failed. Never equate an opened messaging app with a sent or delivered SMS.
- **Offline data:** imported records are structurally validated but their source metadata is not cryptographically authenticated. Every dataset needs independently checked source, license, version, coverage, retrieval time, freshness, and integrity reference.
- **Security:** encrypt sensitive data at rest; keep secrets out of logs and notifications; define key loss, rotation, backup, reinstall, and recovery before production.
- **Mesh:** discovery is not connection; a local queue is not radio transmission; a peer ACK is not emergency-service acknowledgement. Authenticated peer identity/key establishment must precede trusted packet/ACK exchange.
- **Permissions and lifecycle:** every platform feature must handle denial, revocation, disabled hardware, process death, reboot, and unsupported devices.
- **Accessibility:** reduced motion, contrast, text scaling, TalkBack, large targets, and emergency-action priority are acceptance requirements, not decorative extras.
- **Release:** current-head CI must match the exact candidate SHA. Device/data/security acceptance is tracked separately and must not be inferred from CI.

## Current implementation boundary (2026-11-02)

The repository contains a native Android scaffold and partial implementations including versioned consent/privacy UI, SOS preparation and messaging-app handoff, local persistence, selected encrypted fields and PIN vault, location helpers, voice-note handling, local directory/advisory/evacuation caches and bounded JSON import, map-tile cache metadata, BLE discovery, mesh packet/queue primitives, and a visible SOS handoff status service. The consent notice has an independently scrollable body with a fixed Accept/Decline action area, safe-drawing insets, explicit policy-version acceptance, and a main-screen privacy-review/withdraw-consent control. The current candidate adds Robolectric consent-store tests; compact-screen, large-font, landscape, TalkBack, and real-device UI acceptance remain pending. The last successful pre-consent-change CI was commit `90dbcf76d04c925e7734d035cb294c6350351264`, run [41387038466](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/41387038466), with lint, unit tests, debug/release assembly, exact-SHA verification and APK/evidence upload. Artifact `ph-resq-apk-builds` ID `13116156450` contains both APK variants and build evidence; it expires 2026-11-16. The current consent/UI candidate is `232f4093bf8d8c8ba11398b8ce67d6ccb6e3561f`; its CI run is pending/in progress and no APK from that candidate is approved yet. The Android manifest and launcher resources reference the canonical PH-ResQ label/icon resources; physical installer/launcher verification remains part of device acceptance. This is not physical-device acceptance.

The following remain incomplete or unverified: complete six-area persistence/recovery, nationwide authoritative directory, licensed offline map package and renderer, live PAGASA/LGU ingestion, signed dataset manifest/update/rollback, SMS delivery receipt, authenticated peer-key establishment, BLE payload transport, Wi-Fi Direct, multi-hop mesh, remote family sync, duress vault, full accessibility/security/legal review, and physical-device acceptance.

Update this section whenever the evidence in `IMPLEMENTATION_STATUS.md` changes. Keep the PR open and do not merge or publish a production release without explicit authorization.
