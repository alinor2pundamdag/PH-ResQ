# PH-ResQ Implementation Status

Snapshot: 2026-10-27. Current status: early Android scaffold; not a release candidate.

| Area | Status | Evidence / limitation |
|---|---|---|
| Android project and CI definition | Added; CI pending | Gradle Android application module and GitHub Actions workflow added in this branch |
| Consent gate | Basic local preference implemented | Requires lifecycle/UI tests |
| SOS press-and-hold | Code path added; CI/device validation pending | Pointer hold timeout is set to 2 seconds; timing, cancellation, accessibility, and accidental activation still require tests |
| Emergency contact list | Basic local input with Android Keystore AES-GCM at-rest encryption | Not a PIN vault; key-loss/reinstall/recovery and tamper tests remain pending |
| SMS | User-confirmed messaging-app handoff | Recipient URI behavior requires physical-device verification; no delivery confirmation; last draft and state are locally persisted |
| Location in SOS | Last-known location helper added; permission/device validation pending | Reads only a cached last-known fix; no fresh GPS acquisition or accuracy/staleness threshold enforcement |
| Voice note | Encrypted local recording added; CI/device validation pending | Max 10 seconds/2 MB and AES-GCM at rest; no playback or SMS attachment, so F-006 remains partial |
| Family check-in | Local-only | No network relay or family delivery |
| Offline directory/maps | Not implemented | No verified dataset or licensed map package |
| BLE peer discovery | Partial — native 15-second low-power scan added; CI/device validation pending | Discovery only; no authenticated packet exchange or multi-hop mesh |\n| Wi-Fi Direct/multi-hop mesh | Not implemented | No native transfer/protocol implementation |
| Encrypted PIN vault/duress mode | Not implemented | No security claim made |
| PAGASA/evacuation data/cell-broadcast awareness | Not implemented | Sources/licensing not validated |
| Offline national hotline subset | Added from official government portal | Searchable agency list with dialer/SMS handoff; reviewed 2026-10-27; local coverage and successful response not guaranteed |\n| Local SQLite schema | Added | Transactional SOS event history plus schema foundations for location, directory, map metadata, search, bookmarks, and check-ins; full data layers/migrations are incomplete |\n| Automated unit tests | Added; CI pending | Contact parsing, message content, state transition tests |
| Physical-device tests | Not run | Requires Android devices, SIM/network, permissions, field verification |
| Release build/signing | Not ready | No signing configuration or release evidence |

A manifest permission, UI card, or declared interface is not evidence of a completed feature. Do not distribute this early build as a dependable emergency service.
