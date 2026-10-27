# PH-ResQ Release Candidate Checklist

Current decision: NO-GO. This is an early Android scaffold, not a validated emergency application.

## Build and engineering
- [ ] CI unit tests pass on the exact candidate SHA.
- [ ] Debug APK build passes and artifact is retrievable.
- [ ] Release variant builds reproducibly from a clean checkout.
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
