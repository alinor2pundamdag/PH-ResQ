# PH-ResQ Release Candidate Checklist

Current decision: NO-GO. This is an early Android scaffold, not a validated emergency application.

## Build and engineering
- [x] Historical baseline commit `408e2aaaea09df70400c92038b405c5d5a47d337` passed unit tests and debug/release assemble ([run](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40451629467)); this is not evidence for the current candidate.
- [ ] Current PR head passes `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease` in one CI run.
- [ ] CI build metadata proves the APK was built from the exact PR-head commit.
- [ ] Debug APK and SHA-256 checksum artifact are available from the same successful current-head run.
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
