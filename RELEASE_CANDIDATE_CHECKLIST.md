# PH-ResQ Release Candidate Checklist

Current decision: **NO-GO for production release.** Controlled APK testing is allowed only from the artifact attached to the latest successful PR-head CI run after confirming that the run `head_sha` matches the intended PR commit and `BUILD_METADATA.txt` reports the same source SHA. Historical run success does not validate later changes. This is not a validated emergency service; the release APK is unsigned.

## Build and engineering
- [x] Historical baseline commit `408e2aaaea09df70400c92038b405c5d5a47d337` passed unit tests and debug/release assemble ([run](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40451629467)); this is not evidence for the current candidate.
- [x] Latest validated PR-head workflow [41380309779](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/41380309779) completed successfully for `cfef2855f69c5605ee1d3bcaf4175d3fbfcbf429`.
- [x] CI verified `BUILD_METADATA.txt` source/checkout SHAs match that exact commit.
- [x] Debug/release APKs and checksum evidence uploaded in artifact `ph-resq-apk-builds` (ID `13112892154`, expires 2026-11-16); archive digest `90f3b6b948be5120192f56ab67a619477632f0fa8ef5272b0736dc1db9010ef5`.
- [ ] Latest source commit `c087b936522a08ee2bbc4944c2349cf0cbb6e77b` must pass exact-head CI before any APK from it is installed.
- [ ] Re-run after canonical artifact-name alignment and use only that final successful run for the one-session device test.
- [ ] Release APK signing configured and verified; current release variant is unsigned.
- [ ] Release variant builds reproducibly from a clean checkout with documented local build instructions.
- [ ] Dependency versions, SDK levels, and release notes are reviewed.
- [ ] No secrets, signing keys, personal data, or debug endpoints committed.

## Critical emergency behavior
- [ ] Two-second SOS hold is measured; accidental activation and cancellation tested.
- [ ] Contact list persistence and validation tested on device.
- [ ] SMS intent and recipient behavior tested across supported messaging apps and SIM/carrier combinations.
- [ ] Location permission, accuracy, stale-location labeling, and fallback implemented/tested.
- [ ] SOS persistence, retry, and truthful delivery states tested.
- [ ] Voice note recording, encryption, size limits, deletion, and playback tested.

## Offline data and maps
- [ ] JSON import tested with official-source sample data; metadata is reviewed manually because it is not cryptographically signed.
- [ ] Nationwide administrative data source and coverage audited.
- [ ] Helpline sources and jurisdiction/review dates verified.
- [ ] Offline map redistribution rights documented.
- [ ] Package size, update, integrity, rollback, and stale-data behavior measured.
- [ ] PAGASA and evacuation data ingestion/freshness tested.

## Security, privacy, and resilience
- [ ] Encrypted vault/key lifecycle/nonce uniqueness/lockout/recovery reviewed and tested.
- [ ] BLE/Wi-Fi Direct authentication, TTL, replay prevention, deduplication, retries, and limits implemented/tested.
- [ ] Permission denial, process death, reboot, low storage, corrupted database, and interrupted update tests pass.
- [ ] Accessibility/TalkBack/large text/contrast/reduced-motion tests pass.
- [ ] Privacy notice, consent, retention, data sharing, and legal/policy review completed.

## Device and release evidence
- [ ] Critical paths pass on minimum Android API and representative target devices.
- [ ] Physical-device mesh interoperability tested if mesh remains in scope.
- [ ] Battery/performance tests run on low-end supported hardware.
- [ ] Signed release artifact, checksum, version, and rollback plan recorded.
- [ ] Residual risks and unsupported behavior disclosed.
- [ ] Final go/no-go approved after evidence review.
