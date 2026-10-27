# PH-ResQ Feature Traceability

**Purpose:** Prevent silent feature removal and make every requirement testable.  
**Status:** Core unit tests and debug/release assemble tasks passed on commit `4a611c8f085cc20ac5ec9558b07074ba2c1b26df` ([CI run](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40450465511)). Physical-device, integration, data-coverage, and full feature acceptance remain outstanding.

| ID | Retained requirement | Planned component/area | Implementation status | Minimum acceptance evidence |
|---|---|---|---|---|
| F-001 | Offline-first core operation | Local storage, cached data, queue | Planned — implementation not evidenced | Core flows work in airplane mode with previously downloaded data |
| F-002 | Nationwide emergency directory down to barangay level | Data import/search | Planned — implementation not evidenced | Source, license, freshness, and coverage audit; missing data surfaced |
| F-003 | Offline maps | Licensed map package/cache | Planned — implementation not evidenced | Tested offline rendering, coverage/version metadata, package-size report |
| F-004 | Two-second SOS press-and-hold with haptic feedback | SOS UI/state machine | Partial — 2-second hold plus explicit accessible confirmation alternative added; timing, cancellation, haptic and accessibility tests pending | Instrumented timing, accidental activation, accessibility and haptic tests |
| F-005 | Tagalog SMS to up to 10 emergency contacts with location | SOS dispatcher | Partial — messaging-app handoff; no confirmed delivery; recipient URI/device tests pending | Tests for permission/SIM/network failures and honest send/handoff status |
| F-006 | Voice note attached to SOS | Recorder/attachment queue | Partial — encrypted local recording added; playback/attachment and device tests pending | Permission denial, cancellation, size limit, persistence and playback tests |
| F-007 | Six persistence layers | Local persistence layer | Partial — encrypted contacts and SOS-state persistence only; other areas incomplete | Recovery tests for active mode, contact vault, last GPS, SOS, history/bookmarks, map tiles |
| F-008 | Low-battery snapshot below 5% | Persistence/lifecycle | Planned — implementation not evidenced | Best-effort callback test; critical transitions persisted independently of callback |
| F-009 | SHA-256 integrity verification | Data integrity manager | Partial — SHA-256 verifier added and unit-tested in CI; trusted manifest and recovery pipeline pending | Trusted expected digest, tamper detection, verified recovery path |
| F-010 | Family “Ligtas Ako” check-in and sync queue | Check-in/sync | Partial — local SQLite check-in persistence added; no sync/delivery | Offline queue, deduplication, stale/pending/synced/failure states |
| F-011 | Adaptive GPS polling | Location service | Partial — last-known helper and interval policy unit-tested in CI; no lifecycle-aware polling service/fresh fix | Battery/lifecycle/permission tests and timestamp/accuracy display |
| F-012 | BLE peer discovery | Native BLE module | Partial — 15-second low-power discovery scan added; permission/device tests pending | Physical-device scan/permission/lifecycle tests; no placeholder success |
| F-013 | Wi-Fi Direct transfer | Native P2P module | Planned — implementation not evidenced | Pairing, transfer, interruption, retry and device-matrix tests |
| F-014 | Multi-hop emergency mesh | Mesh protocol/router | Partial — authenticated envelope codec and bounded-hop/dedupe utilities unit-tested in CI; radio transport, durable replay cache, and acknowledgements pending | TTL, hop limit, acknowledgement, duplicate, replay, malformed-packet tests |
| F-015 | Geohash/spatial lookup | Local data layer | Partial — geohash encoder unit-tested in CI; indexed search and realistic benchmarks pending | Indexed or benchmarked query strategy with realistic data and measured latency |
| F-016 | Rolling privacy tokens | Security/mesh identity | Partial — HMAC time-window token utility unit-tested in CI; key lifecycle and clock-drift integration pending | Rotation, collision, clock drift, replay and linkability tests |
| F-017 | Encrypted PIN-protected vault | Security vault | Partial — contact-field encryption only; no PIN-protected vault/key recovery | Encryption, key lifecycle, lockout/rate-limit, backup/reinstall and recovery tests |
| F-018 | Optional duress PIN/dummy vault | Threat-modelled security module | Planned — implementation not evidenced | Threat-model review; no absolute secrecy or physical-safety guarantee claims |
| F-019 | Stealth audio mute | SOS/audio policy | Planned — implementation not evidenced | Silent-mode test; visual status and cancellation remain available |
| F-020 | Offline Medical ID QR | Medical ID/share UI | Planned — implementation not evidenced | Explicit field selection; QR payload minimization and disclosure warning |
| F-021 | High-contrast accessibility | UI/accessibility | Planned — implementation not evidenced | Contrast, TalkBack, scalable text, touch-target and reduced-motion tests |
| F-022 | Foreground service notification/actions | Android lifecycle service | Planned — implementation not evidenced | Target-SDK compliance and device tests; visible required notification |
| F-023 | Safe volume policy | Audio/accessibility | Planned — implementation not evidenced | User settings, stealth mode, Android audio behavior tests |
| F-024 | Verified helpline fallback | Directory/fallback UI | Partial — 911 dialer fallback verified; no nationwide/local directory | Every number has authoritative source/jurisdiction and review date |
| F-025 | PAGASA alert cache | Advisory cache | Planned — implementation not evidenced | Source, publication/retrieval time, expiry and stale-cache labeling |
| F-026 | Evacuation centre status | Local directory/update flow | Planned — implementation not evidenced | Source/time metadata, stale/missing status and update-failure tests |
| F-027 | Cell-broadcast awareness | Help/information flow | Planned — implementation not evidenced | No claim that a third-party app intercepts all system/carrier broadcasts |
| F-028 | Privacy/permission compliance | Consent/privacy/settings | Partial — basic consent gate; complete privacy/legal/permission audit pending | Permission rationale, denial path, data minimization and policy review |
| F-029 | Chaos/resilience testing | Test suite | Planned — implementation not evidenced | Process death, reboot, corrupt storage, low space, no network/GPS/SIM, duplicate/out-of-order packets |
| F-030 | Philippine flag color-looping title and waving visual | UI/theme/animation | Planned — implementation not evidenced | Readability, reduced-motion, background/battery and emergency-control usability tests |
| F-031 | 15-second mesh battery-saver scan window | Mesh scheduler | Planned — implementation not evidenced | Measured scan duty cycle/battery behavior; document OS scheduling limits |
| F-032 | Search and bookmark history | Local data layer | Planned — implementation not evidenced | Persistence, clear-history/privacy controls and migration tests |
| F-033 | SMS/mesh delivery state | SOS/mesh state machine | Partial — state/draft persistence; SMS delivery confirmation absent | Queued/sending/sent-to-handoff/delivered/acknowledged/failure states tested |
| F-034 | Nationwide map and directory updates | Data pipeline | Planned — implementation not evidenced | Source license, version, freshness, size, update rollback and incomplete-update tests |

## Status vocabulary
- **Planned:** requirement retained, implementation not evidenced.
- **Partial implementation:** some code exists, but one or more acceptance criteria remain unmet; do not count as complete.
- **Implemented:** code reference exists and review is complete.
- **Tested:** required test passed on a named revision/device/environment with evidence.
- **Blocked:** dependency or platform limitation prevents acceptance.
- **Accepted with limitation:** test evidence exists and the limitation is explicitly documented.

No requirement is considered implemented or tested merely because it appears in a plan or sample code.
