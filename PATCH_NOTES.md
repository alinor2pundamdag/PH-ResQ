# PH-ResQ Architecture Collection — Patch Notes

**Collection revision:** 2026-10-28  
**Status:** Documentation consolidation on the existing feature branch; not merged to `main`.

## Purpose

This patch restores the missing standalone architecture documents in the 11-document collection and aligns them with the current source tree and existing traceability/audit documents. It does not remove features, claim full acceptance, or authorize merge/release.

## Documents in the core 11-file collection

1. `MASTER_PLAN.md` — scope, constraints and end-to-end product architecture.
2. `WORK_RULES.md` — change control, implementation and verification rules.
3. `ARCHITECTURE_DECISIONS.md` — ADRs and recorded platform decisions.
4. `SOS_STATE_MACHINE.md` — durable SOS state and truthful delivery semantics.
5. `DATABASE_SPEC.md` — schema, migrations, sensitive fields and dataset invariants.
6. `SECURITY_MODEL.md` — assets, trust boundaries, controls and residual risks.
7. `MESH_PROTOCOL.md` — versioned packet format and future transport requirements.
8. `TEST_AND_ACCEPTANCE_PLAN.md` — acceptance rule, P0/P1 tests and stop-ship gates.
9. `IMPLEMENTATION_ROADMAP.md` — ordered gates from correctness to final audit.
10. `README.md` — user-facing project scope and current status.
11. `PATCH_NOTES.md` — this revision's changes and remaining consistency checks.

## Existing companion documents retained

The collection does not replace or delete `PHASE_1_INIT_CONSENT.md` through `PHASE_6_CONCURRENCY_CHAOS.md`, `features_update.md`, `FEATURE_TRACEABILITY.md`, `TEST_ACCEPTANCE_MATRIX.md`, `DATA_SOURCES_AND_RISKS.md`, `IMPLEMENTATION_STATUS.md`, `OFFLINE_DATA_IMPORT.md`, `PRIVACY_NOTICE.md`, or `RELEASE_CANDIDATE_CHECKLIST.md`. These remain supporting specifications and evidence ledgers.

## Corrections and clarifications

- Keep planned, partially implemented, automated-tested, device-accepted and release-ready as distinct statuses.
- SOS messaging-app handoff is not proof of SMS delivery.
- A mesh envelope/queue/ACK utility is not an operational BLE/Wi-Fi Direct mesh until peer-key establishment and radio transport are implemented and tested.
- PIN vault is not a duress vault; QR payloads are not encrypted by default.
- JSON source metadata is not a cryptographic signature and does not certify nationwide coverage.
- SHA-256 only verifies against a trusted expected digest; it does not repair content.
- Cell Broadcast delivery belongs to Android/carrier/government systems; PH-ResQ must not claim it intercepts all broadcasts.
- The foreground SOS handoff notification and its Stop action require physical-device validation, including notification-permission behavior.
- No merge, production deployment, signed release, or claim of complete 34-feature acceptance is authorized by this documentation update.

## Verification still required

1. Review all 34 feature IDs against the current implementation and test matrix.
2. Run current-head CI and ensure artifact metadata/checksum match the exact PR head.
3. Execute P0 physical-device tests for consent, SOS, permissions, process death and messaging handoff.
4. Complete source licensing/coverage, security/privacy, accessibility, and mesh interoperability audits.
