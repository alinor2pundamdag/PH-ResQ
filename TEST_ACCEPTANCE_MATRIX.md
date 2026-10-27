# PH-ResQ Test & Acceptance Matrix

**Automated CI snapshot:** Commit `4a611c8f085cc20ac5ec9558b07074ba2c1b26df` passed `testDebugUnitTest`, `assembleDebug`, and `assembleRelease` ([run](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40450465511)). This does not mean the full matrix passed: unexecuted tests remain open.  
**Rule:** Record repository commit SHA, build variant, device/API, test result, logs/screenshots, and tester for each executed case.

| Test ID | Scenario | Expected result | Evidence required | Gate |
|---|---|---|---|---|
| T-001 | First launch, no consent stored | Consent policy appears before protected features | UI test + persisted state | P0 |
| T-002 | User declines consent and relaunches | Protected navigation remains blocked; app does not claim OS force-stop | UI/lifecycle test | P0 |
| T-003 | User accepts consent then process is killed | Versioned consent remains stored; valid navigation resumes | Instrumented lifecycle test | P0 |
| T-004 | Hold SOS for less than 2 seconds | No dispatch; clear countdown/cancel feedback | UI timing test | P0 |
| T-005 | Hold SOS for required 2 seconds | Exactly one durable SOS event is queued | UI + database test | P0 |
| T-006 | SMS permission denied or no SIM | Clear failure/handoff option; event remains recoverable | Device test | P0 |
| T-007 | SMS accepted by platform but no delivery receipt | Status does not claim recipient delivery | Dispatcher/state-machine test | P0 |
| T-008 | Process death during SOS send | State resumes without silently losing event or creating duplicate send | Fault-injection test | P0 |
| T-009 | GPS unavailable/stale | Last fix includes timestamp/accuracy and is marked stale; manual fallback offered | Device test | P0 |
| T-010 | Storage nearly full or write interrupted | Atomic persistence/recovery behavior; visible error if unable to save | Fault-injection test | P0 |
| T-011 | Tamper with directory database | Trusted digest mismatch is detected; no unverified “self-heal” claim | Integrity test | P0 |
| T-012 | Vault encryption/decryption and wrong PIN | Correct plaintext round-trip; failure throttled; secrets absent from logs | Security test | P0 |
| T-013 | Reinstall/backup/restore vault | Behavior matches documented key recovery policy; no false promise of recovery | Device/security test | P0 |
| T-014 | BLE permission denied / Bluetooth disabled | Scan fails safely and reports unavailable; no fake peer success | Physical-device test | P1 |
| T-015 | BLE peer packet duplicated/replayed/malformed | Duplicate/replay rejected or safely deduplicated; malformed data rejected | Protocol test | P1 |
| T-016 | Wi-Fi Direct connection drops mid-transfer | Bounded retry or recoverable failure; partial payload not accepted as complete | Physical-device test | P1 |
| T-017 | Mesh packet forwarded through peers | TTL/hop limit honored; message ID deduplicated; acknowledgement state accurate | Multi-device test | P1 |
| T-018 | RSSI changes due to wall/device orientation | UI communicates approximate/uncertain proximity, not exact distance | Controlled test | P1 |
| T-019 | Nationwide directory search | Results validate against sourced test dataset; latency measured on low-end target | Benchmark + source audit | P1 |
| T-020 | Offline map coverage | User sees coverage/version and missing-region behavior; licensed tiles render offline | Device + license audit | P1 |
| T-021 | PAGASA cache is stale | Source and timestamps shown; stale content is not presented as current | Unit/integration test | P1 |
| T-022 | Medical ID QR is displayed | Only user-selected fields are included and disclosure is explained | Payload inspection + UI test | P1 |
| T-023 | TalkBack, high contrast, large text, reduced motion | Emergency actions remain operable and readable | Accessibility audit | P1 |
| T-024 | Battery drops below 5% | Best-effort snapshot occurs if process is alive; prior critical transitions already persisted | Device/fault test | P1 |
| T-025 | Reboot, permission revocation, app upgrade | State recovers safely; revoked capability is handled; migrations are validated | Device matrix | P1 |
| T-026 | Family check-in offline then reconnect | Queue is durable, deduplicated and reports pending/synced/failed honestly | Integration test | P1 |
| T-027 | Helpline fallback | Displayed number matches authoritative source, jurisdiction, and review date | Data audit | P0 |
| T-028 | Full test suite on release candidate | No unexplained critical/high failures; residual risks signed off | CI reports + device reports | Release |

## Release gates
- **P0:** consent, SOS integrity, privacy/security baseline, persistence/recovery, honest delivery state, verified emergency fallback.
- **P1:** mesh, nationwide data/maps, accessibility, cache freshness, and broader resilience.
- **Release candidate:** all P0 passed; all P1 passed or explicitly accepted as documented limitations; reproducible build and complete evidence bundle available.

A failed, skipped, unexecuted, or unreported test is not a pass. Older successful builds do not validate a newer revision.
