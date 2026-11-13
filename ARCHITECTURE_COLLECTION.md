# PH-ResQ Architecture Collection

**Collection version:** 1.3  
**Updated:** 2026-11-13  
**Status:** Consolidated architecture index and cross-system audit entry point. The collection describes a partially implemented Android application, not a release-ready emergency service.

## Full system audit

See [FULL_SYSTEM_AUDIT_2026-11-13.md](FULL_SYSTEM_AUDIT_2026-11-13.md) for the latest requirements-to-code audit, all 34 feature statuses, system/device integration findings, P0/P1/P2 risks, exact-head CI status, and remediation order.

The audit baseline is PR #1 head `83902c38132257d540d3f4b2ecc3366915bb886d`; the latest remediation commit is `bc32f658ae366e3a4a7300c290aa26007f4b0b24`. A new SOS recovery change was committed after the baseline audit. No CI run has yet been confirmed for the remediation commit; treat its APK status as NOT VERIFIED until exact-head CI passes.

## Canonical application identity

- Public app name: `PH-ResQ`.
- Brand descriptor only: `Vanguard 2026`.
- Release application ID: `ph.resq.app`; debug may use `.debug`.
- Candidate filenames: `PH-ResQ-debug.apk` and `PH-ResQ-release-unsigned.apk`.
- Do not sign, merge, publish or release without explicit owner authorization.

## Architecture documents

- [MASTER_PLAN.md](MASTER_PLAN.md) — product scope and requirements authority.
- [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) — layers, components and data flows.
- [ARCHITECTURE_DECISIONS.md](ARCHITECTURE_DECISIONS.md) — accepted platform/behavior decisions.
- [CRITICAL_ARCHITECTURE_AUDIT.md](CRITICAL_ARCHITECTURE_AUDIT.md) — prior P0/P1/P2 register.
- [SETTINGS_DEVICE_INTEGRATION_CONTRACTS.md](SETTINGS_DEVICE_INTEGRATION_CONTRACTS.md) — settings, capabilities, policy and UI contracts.
- [ANDROID_COMPATIBILITY_AND_ADAPTIVE_RUNTIME.md](ANDROID_COMPATIBILITY_AND_ADAPTIVE_RUNTIME.md) — Android support tiers and runtime adaptation.
- [PERSISTENCE_RECOVERY_ARCHITECTURE.md](PERSISTENCE_RECOVERY_ARCHITECTURE.md) — six persistence domains and recovery invariants.
- [IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md) — ordered engineering gates.
- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) — implementation and CI evidence snapshot.
- [FEATURE_TRACEABILITY.md](FEATURE_TRACEABILITY.md) — all 34 retained feature IDs.
- [TEST_ACCEPTANCE_MATRIX.md](TEST_ACCEPTANCE_MATRIX.md) — test scenarios and acceptance evidence.
- [SINGLE_PASS_DEVICE_ACCEPTANCE.md](SINGLE_PASS_DEVICE_ACCEPTANCE.md) — one-session physical-device test runbook.
- [RELEASE_CANDIDATE_CHECKLIST.md](RELEASE_CANDIDATE_CHECKLIST.md) — release go/no-go gates.
- [FULL_SYSTEM_AUDIT_2026-11-13.md](FULL_SYSTEM_AUDIT_2026-11-13.md) — current full cross-system audit.

Detailed domain specifications remain authoritative for their own behavior: `SOS_STATE_MACHINE.md`, `DATABASE_SPEC.md`, `SECURITY_MODEL.md`, `MESH_PROTOCOL.md`, `DATA_SOURCES_AND_RISKS.md`, `OFFLINE_DATA_IMPORT.md`, and `PHASE_1` through `PHASE_6`.

## Architecture boundary

Current app is Kotlin + Jetpack Compose, Android API 26+, compile/target SDK 35, Java 17. BLE discovery and mesh queue/codec primitives are not operational mesh transport. SMS is a user-confirmed messaging-app handoff, not delivery. Offline JSON import is not source authentication or nationwide coverage. The release APK is unsigned; device, accessibility, security, data and legal acceptance remain separate gates.
