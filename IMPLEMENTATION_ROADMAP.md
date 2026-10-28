# PH-ResQ Implementation Roadmap

**Status:** Ordered plan, not a claim that every phase is complete. Preserve all feature IDs in `FEATURE_TRACEABILITY.md`; update this roadmap whenever implementation evidence changes.
**Execution policy:** Work in small auditable commits on the feature branch. Do not merge or publish a production release without authorization.

## Gate 0 — Source-of-truth and consistency
- Reconcile this collection with `MASTER_PLAN.md`, `WORK_RULES.md`, the six phase files, `FEATURE_TRACEABILITY.md`, `TEST_ACCEPTANCE_MATRIX.md`, `DATA_SOURCES_AND_RISKS.md`, and implementation status.
- Resolve stale statements about existing source (PIN vault, voice-note, mesh queue, dataset import, foreground notification) without promoting utilities to complete end-to-end features.
- Require an ADR for any platform-impossible or conflicting requirement; retain user-facing intent and document the alternative.

**Exit evidence:** no contradictions in feature status, named source references exist, all 34 feature IDs remain represented.

## Gate 1 — P0 emergency correctness
- Finish durable SOS event orchestration and prove persistence before handoff.
- Verify the two-second hold, accessible alternative, cancel behavior, duplicate prevention, stale/missing location, honest status wording and notification lifecycle.
- Add tests for process death, no SIM, no messaging app, notification permission denial, interrupted write and retry behavior.

**Exit evidence:** all P0 SOS/consent tests pass on current SHA and representative physical devices; no false delivery claims.

## Gate 2 — Persistence, recovery and privacy
- Audit every sensitive field and key lifecycle; define retention, deletion, backup/reinstall, key loss and migration policy.
- Complete repository wiring and database migrations; add migration fixtures and rollback tests.
- Finish family check-in semantics and voice-note lifecycle/retention; ensure notifications, QR, logs and shares disclose only user-selected information.

**Exit evidence:** migration matrix, cryptographic tests, retention policy, backup/recovery decision and device evidence.

## Gate 3 — Directory, advisories and maps
- Obtain authoritative sources and rights; build a provenance inventory with version, license, retrieval date, geographic coverage and update cadence.
- Validate user-selected JSON import, record counts, duplicates, invalid coordinates and transactional replacement.
- Bundle and license an offline map package and renderer, if legally and technically viable; otherwise clearly state map limitation and preserve a future implementation plan.

**Exit evidence:** source/license review, coverage report, offline test, package size and freshness behavior.

## Gate 4 — Real mesh transport
- Finalize peer identity/key establishment and protocol versioning.
- Implement and test actual BLE exchange, then Wi-Fi Direct transport, with bounded queue, ACK, dedupe, TTL, retry, rate limits and battery policy.
- Validate supported device/API matrix and threat model. Do not claim mesh delivery from discovery or queue primitives.

**Exit evidence:** multi-device tests across supported combinations, authenticated ACK evidence, packet-fuzz/replay tests, battery report.

## Gate 5 — UI/UX and accessibility
- Audit every primary screen and emergency flow for contrast, touch targets, TalkBack, font scaling, high contrast, reduce motion, status clarity and low-end device performance.
- Verify visual effects never block or delay emergency actions; test offline/stale/no-permission/error states.

**Exit evidence:** screen-by-screen checklist, accessibility test results and physical-device screenshots with synthetic data.

## Gate 6 — Data governance, security and resilience
- Verify hotline and local contacts; finish privacy notice, retention/deletion, threat-model review and legal review.
- Execute chaos tests: process kill, reboot, storage exhaustion, corrupt data, clock drift, permission revocation, network changes and interrupted migrations.
- Review dependency and release-signing process; keep keys out of Git/CI logs.

**Exit evidence:** security review, data-source audit, resilience report and open-risk register.

## Gate 7 — Candidate build and final audit
- Run lint, all unit tests, debug/release builds and artifact checks on the exact candidate SHA.
- Verify artifact metadata and checksum, unsigned/signed status, versioning, dependency state, and clean-checkout reproducibility.
- Run the full feature traceability review: every feature is implemented, tested, explicitly partial, or blocked with owner and evidence.

**Exit evidence:** current-head CI pass, matching APK artifact + SHA-256, completed acceptance matrix, documented residual risks. If any P0 gate fails, decision remains NO-GO.

## Status snapshot
The repository contains substantial partial Android source and utility tests. Current limitations include no complete nationwide data/map package, no operational BLE/Wi-Fi Direct mesh transport or peer-key establishment, no recipient-delivery confirmation for messaging-app handoff, and incomplete physical-device/accessibility/security/recovery acceptance. CI success is not equivalent to feature acceptance.
