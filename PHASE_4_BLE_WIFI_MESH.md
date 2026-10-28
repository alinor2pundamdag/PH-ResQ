# Phase 4 — BLE, Wi-Fi Direct & Offline Emergency Mesh

**Status:** BLE discovery and version-1 envelope/durable queue primitives exist. Authenticated peer-key establishment, BLE payload transport, Wi-Fi Direct transfer, multi-hop forwarding, interoperability, and battery acceptance remain unimplemented or unverified.

## Objective
Preserve peer discovery and local emergency transfer during network outages while protecting privacy and preventing duplicate, stale, replayed, or malformed messages.

## 1. Platform implementation
Use Android BLE and Wi-Fi Direct APIs through the selected native architecture. Runtime permissions, Bluetooth/Wi-Fi state, lifecycle, device restrictions, and background execution must be handled explicitly. A permission function that always returns true, log-only scanning, or send function that always reports success is not an implementation.

## 2. Packet envelope
Before implementing routing, define a versioned envelope with:
- protocol version and message type;
- stable per-message ID and creation timestamp;
- rotating sender pseudonym (not a permanent device identifier);
- TTL and hop limit;
- payload type and strict maximum size;
- authenticated integrity tag/signature as appropriate;
- acknowledgement/correlation ID where applicable.

Do not place precise GPS coordinates, contact lists, or medical data in unauthenticated BLE advertisements. Exchange sensitive payloads only after an authenticated connection and appropriate user consent.

## 3. Delivery and routing semantics
- Define direct-send and forwarding behavior, peer eligibility, queue limits, and expiry.
- Acknowledge receipt only at the layer actually confirmed; distinguish received, validated, stored, forwarded, and delivered states.
- Use bounded retries with exponential/backoff policy and deduplication by message ID.
- Enforce TTL/hop limit, replay resistance, malformed-packet rejection, rate limits, and maximum payload size.
- Handle clock drift and rolling-token rotation without making message expiry or authentication unreliable.
- Never report “delivered” merely because a socket write returned success.

## 4. BLE proximity and RSSI
RSSI is an approximate radio-strength measurement, not an exact distance sensor. Walls, orientation, antenna variation, interference, and device differences change results. Display a rough estimate/uncertainty or “unknown”; do not claim exact meters.

Trilateration is conditional on enough known reference positions, suitable geometry, and acceptable measurement error. Without these conditions, do not compute or display a precise peer position.

## 5. Wi-Fi Direct and payload transfer
Use an authenticated handshake, bounded payload transfer, integrity verification, timeout, and partial-transfer recovery. Voice notes and Medical ID data must remain protected and size-limited. Test connection setup, transfer interruption, peer departure, duplicate delivery, and incompatible devices.

## 6. Battery-saver policy
Preserve the requested 15-second scan window as a configurable duty-cycle policy. Android may defer or constrain background work, so do not promise exact scheduling. Measure battery usage and stop scanning when disabled, permission is revoked, or no longer needed.

## Current implementation boundary
`BleDiscoveryManager`, `MeshEnvelopeCodec`, and `DurableMeshQueue` provide discovery/codec/queue primitives only. Do not claim that an SOS packet is transmitted between devices or delivered end-to-end until an authenticated transport and physical multi-device tests exist. See `MESH_PROTOCOL.md`.

## Acceptance criteria
- Physical-device tests demonstrate actual discovery and authenticated transfer.
- Permission denial and disabled radios report unavailable, never fake success.
- Duplicate, replayed, expired, malformed, and oversized packets are safely rejected/deduplicated.
- TTL/hop limit, acknowledgement, retry/backoff, and delivery states are tested.
- Battery measurements and supported-device matrix are recorded.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-014 through T-018.
