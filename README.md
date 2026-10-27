# PH-ResQ Vanguard 2026 🇵🇭

> **Android-first, offline-first emergency assistance project for the Philippines.**

**Current repository status:** An initial Kotlin/Jetpack Compose Android scaffold and Android CI workflow have been added on the implementation branch alongside the planning documents. Build/test results are pending. This is not a complete or release-ready emergency application; feature descriptions below remain requirements unless implementation and evidence are explicitly recorded in IMPLEMENTATION_STATUS.md.

## Layunin

Nilalayon ng PH-ResQ Vanguard 2026 na magbigay ng offline-capable emergency directory, SOS workflow, location sharing, family check-in, offline maps, and disaster-resilience tools. Ang lahat ng capability ay kailangang mapatunayan sa source code, automated tests, physical-device tests, at verified data sources bago sabihing gumagana o release-ready.

## Retained feature scope

- Offline-first operation, local emergency directory, barangay-level search, and licensed offline maps.
- Two-second SOS press-and-hold with haptic feedback and safe accidental-activation handling.
- Tagalog SMS workflow for up to 10 configured emergency contacts, location timestamp/accuracy, status tracking, and voice-note support.
- Six persistence areas: active mode, encrypted emergency-contact vault, last known GPS, crisis SOS state, search/bookmarks, and offline map tiles.
- Best-effort low-battery snapshot below 5%; critical state is saved at each transition and does not rely solely on this callback.
- SHA-256 integrity checking against a trusted expected digest/manifest and verified recovery source.
- Family “Ligtas Ako” check-in and durable sync queue.
- Adaptive GPS polling, BLE peer discovery, Wi-Fi Direct transfer, multi-hop mesh protocol, TTL/acknowledgements/retries/deduplication, and rolling privacy tokens.
- Geohash/spatial search with a documented index/query strategy and measured performance.
- Encrypted PIN vault, Android Keystore where feasible, optional duress PIN/dummy vault with documented threat-model limits, and stealth audio mute.
- Offline Medical ID QR with user-selected minimal fields and explicit disclosure warning.
- High-contrast accessibility, scalable text, TalkBack support, reduced motion, safe volume policy, foreground-service notification/actions, and lifecycle recovery.
- Verified emergency helpline fallback, timestamped PAGASA advisory cache, evacuation-centre status, and cell-broadcast awareness that respects Android/carrier limitations.
- Privacy/permission compliance, data-source licensing and freshness, and chaos testing for failures and interrupted operations.
- Philippine-flag color-looping title and waving visual identity, without obscuring emergency controls or compromising accessibility.

## Architecture baseline

- Android-native APIs are required for location, SMS, notifications, foreground services, BLE, Wi-Fi Direct, Keystore, and lifecycle-sensitive work.
- Browser-only APIs such as `window`, `localStorage`, IndexedDB, and DOM canvas are not substitutes for Android capabilities.
- Initial UI/build baseline: Kotlin + Jetpack Compose, minSdk 26, compileSdk/targetSdk 35, Java 17. CI must validate this baseline. Database, verified data sources, and mesh protocol remain open implementation decisions.
- RSSI is approximate; SMS/mesh delivery is not guaranteed; ordinary apps cannot force-stop themselves or intercept every cell broadcast.
- Nationwide counts, map coverage, licenses, package size, and freshness must be verified from authoritative sources.

## Implementation and release status

- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) — evidence-backed implementation status and limitations
- [RELEASE_CANDIDATE_CHECKLIST.md](RELEASE_CANDIDATE_CHECKLIST.md) — release gate; currently NO-GO
- GitHub Actions workflow: `.github/workflows/android-ci.yml` — unit tests, debug APK build, and APK artifact upload

The initial app currently includes a consent gate, Android Keystore AES-GCM at-rest encryption for the contact field, a two-second hold interaction (validation pending), last-known location lookup (permission/device validation pending), a national 911 dialer fallback sourced from the official Philippine Government Emergency Hotlines portal, a user-confirmed SMS handoff, and locally persisted SOS state/draft. It does not yet include fresh GPS acquisition, voice notes, offline datasets/maps, a PIN-protected vault, BLE/Wi-Fi Direct mesh, or remote family sync. Do not rely on this scaffold as a validated emergency service.

## Documentation map

- [MASTER_PLAN.md](MASTER_PLAN.md) — master scope, architecture, and release gate
- [WORK_RULES.md](WORK_RULES.md) — repository and implementation safeguards
- [features_update.md](features_update.md) — retained feature inventory and status rules
- [ARCHITECTURE_DECISIONS.md](ARCHITECTURE_DECISIONS.md) — architecture decisions and open questions
- [FEATURE_TRACEABILITY.md](FEATURE_TRACEABILITY.md) — feature-to-test traceability
- [TEST_ACCEPTANCE_MATRIX.md](TEST_ACCEPTANCE_MATRIX.md) — measurable acceptance tests
- [DATA_SOURCES_AND_RISKS.md](DATA_SOURCES_AND_RISKS.md) — data provenance, licensing, and risk register
- [PHASE_1_INIT_CONSENT.md](PHASE_1_INIT_CONSENT.md) through [PHASE_6_CONCURRENCY_CHAOS.md](PHASE_6_CONCURRENCY_CHAOS.md) — phased specifications

## Working phases

1. Confirm Android architecture, data sources, threat model, and acceptance criteria.
2. Foundation, initialization, privacy policy, consent, and permissions.
3. Local database, six persistence areas, directory, integrity, and offline maps.
4. SOS, location, SMS/handoff, voice note, family check-in, and status machine.
5. Encrypted vault, PIN handling, rolling tokens, QR disclosure, and threat-modelled duress mode.
6. BLE/Wi-Fi Direct protocol, lifecycle/battery safeguards, integration, concurrency, and chaos tests.
7. Accessibility, visual identity, safe volume, foreground service, and device safeguards.
8. Full integration, data validation, physical-device matrix, and release evidence.

## Status vocabulary

- **Planned:** requirement retained; implementation not evidenced.
- **Implemented:** code reference exists and has been reviewed.
- **Tested:** named revision and device/environment have passing evidence.
- **Blocked:** dependency or platform limitation prevents acceptance.
- **Release-ready:** every required release gate has evidence and residual risks are disclosed.

A plan, sample snippet, or older successful build does not prove that the current revision works.