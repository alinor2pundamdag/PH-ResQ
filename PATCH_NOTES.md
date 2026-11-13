# PH-ResQ Architecture Collection — Patch Notes

**Collection revision:** 2026-11-02  
**Status:** Architecture collection consistency correction on the existing feature branch; PR #1 remains open and unmerged.
**Latest verified PR head before this documentation-only correction:** `99457bdc4d07214be1bf3e4fbb581cd64c8fc061` passed CI run [41378995667](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/41378995667). Any new commit created by this correction requires its own CI result before it is build-validated.

## 2026-11-02 — Revalidate latest Android candidate

- Latest PR #1 head `99457bdc4d07214be1bf3e4fbb581cd64c8fc061` passed CI run [41378995667](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/41378995667).
- CI executed `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; logs report `BUILD SUCCESSFUL in 4m 7s`.
- Exact source/checkout SHA verification and APK checksum/build-evidence generation succeeded.
- Artifact `ph-resq-apk-builds` ID `13112418255` was uploaded; archive SHA-256 is `9ba182f9a8bad038d454af74c54ca89a36388ef569e6f26a71e4525cce3576cf`, expires 2026-11-16. Release APK remains unsigned.
- This is build/test evidence only. Physical-device, nationwide data, map renderer/licensing, Wi-Fi Direct, operational mesh, delivery, recovery, and accessibility acceptance remain open.

## 2026-11-02 — Architecture collection consistency correction

- Reconciled the implementation status, release checklist, and acceptance matrix with the actual open PR head and latest successful current-head CI run.
- Removed superseded commit/run/artifact references from current-status sections; preserved the rule that older successful runs do not validate newer commits.
- Removed a duplicate acceptance-matrix rule and advanced the collection revision to 1.1.
- Confirmed PR #1 remains open/unmerged and the release APK remains unsigned; this documentation update does not authorize merge or release.

## 2026-11-02 — Canonical application naming

- Standardized the public application name to exactly `PH-ResQ` from pre-download listing through downloaded APK, Android installer, installed launcher, and in-app title.
- `Vanguard 2026` is retained only as a brand descriptor, not part of the application name.
- Added `@string/app_name` as the Android label source of truth and stable artifact names `PH-ResQ-debug.apk` and `PH-ResQ-release-unsigned.apk`.
- Kept release application ID `ph.resq.app`; the existing debug suffix remains a technical side-by-side install identifier.
- Current-head CI must verify the generated artifact filenames and manifest label before this naming change is accepted.

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

- Added a document-authority and dependency flow to `MASTER_PLAN.md`, including a rule that device capability limits require graceful fallback or a clear unsupported state rather than silent feature removal.
- Aligned `WORK_RULES.md` with that authority order and preserved the 34-feature scope.
- Corrected F-007's persistence status to acknowledge the existing SQLite-backed state/history/bookmark/check-in/cache/mesh tables while keeping full lifecycle recovery and acceptance open.
- Expanded `TEST_ACCEPTANCE_MATRIX.md` with missing acceptance cases and an explicit F-001–F-034 → test-ID dependency map; mapped tests are requirements, not claims of execution/pass.

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


## 2026-11-13 — Architecture collection expansion and audit
- Added a source/document audit and explicit P0/P1/P2 remediation register.
- Added Android capability-based compatibility and adaptive runtime policy; clarified API 26+ baseline and non-universal OEM behavior.
- Added settings/device capability/policy integration contracts.
- Added six-domain persistence, key lifecycle, migration, retention and recovery architecture.
- Updated collection index, master plan, system architecture, ADRs, roadmap, work rules and status snapshot. All 34 retained feature IDs remain in scope.
- Audit SHA: `0ef2401d0cdfb71ed34c6dbe4e3b3f746aee6f5d`. No source runtime behavior changed in this documentation pass.


## 2026-11-13 — SOS event durability and receipt-state hardening
- Persisted the exact encrypted SOS draft, recipient snapshot and location in QUEUED before launching the messaging app; persisted SENDING before the external side effect.
- Added stable UUID event IDs and a non-destructive schema v7 → v8 migration that assigns separate IDs to legacy rows.
- Enforced legal per-event state transitions at the database boundary.
- Generic transitions now reject DELIVERED and ACKNOWLEDGED until trusted delivery/recipient-ack evidence exists.
- Added Robolectric coverage for snapshot persistence, stable IDs, receipt-state rejection and schema migration. Current-SHA CI and device acceptance remain required.
