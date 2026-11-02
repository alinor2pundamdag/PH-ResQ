# Phase 4: optional proximity research

**Research track only; no BLE or Wi-Fi Direct implementation exists here. Do not rely on it for SOS.**

1. Implement native platform adapters only after verifying API-level permissions, device support, background restrictions and user consent. A recurring timer is not a scanner; test start/stop, cancellation and cleanup.
2. Measure battery use with bounded scan windows and throttling. Do not infer exact distance or trilateration from uncalibrated RSSI; report uncertainty.
3. Authenticate peers and encrypt packets with replay protection, deduplication, hop limits, size limits and expiry. Define clear outcomes for discovery, transport attempt and acknowledgement; a send attempt is not delivery.
4. Test offline peer discovery and multi-hop under radio interference, revoked permissions, device sleep and zero peers. Avoid sensitive payloads until threat review.

**Acceptance gate:** field test on multiple real Android models with measured delivery/failure and battery results; core SOS remains usable without this module.
