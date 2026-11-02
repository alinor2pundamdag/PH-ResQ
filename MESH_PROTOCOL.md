# PH-ResQ BLE / Wi-Fi Direct Mesh Protocol

**Status:** Version-1 packet and durable queue primitives exist; operational peer authentication, BLE payload exchange, Wi-Fi Direct transport, multi-hop service, and device-matrix validation are not implemented.
**Related:** `ARCHITECTURE_DECISIONS.md`, `SECURITY_MODEL.md`, `DATABASE_SPEC.md`, `TEST_AND_ACCEPTANCE_PLAN.md`.

## 1. Goals and non-goals

The future mesh should carry small, bounded emergency messages across nearby devices when ordinary connectivity is unavailable. It must be opportunistic, power-aware, privacy-preserving, authenticated, replay-resistant, and explicit about delivery uncertainty. It is not a replacement for emergency services, cellular networks, or verified recipient acknowledgement.

Current `MeshEnvelopeCodec` and `DurableMeshQueue` provide packet encoding/validation, HMAC integrity, queue persistence, retry state, ACK verification, and replay primitives. They do not establish trust between peers or transmit packets over BLE/Wi-Fi Direct.

## 2. Version 1 envelope

The codec serializes the following fields and appends a 32-byte HMAC-SHA256 tag:

| Field | Purpose / validation |
|---|---|
| Magic | ASCII bytes `PHRQ` |
| Version | Integer; currently version 1 only |
| Message ID | Non-empty, maximum 64 characters; globally collision-resistant ID required |
| Sender token | Non-empty, maximum 64 characters; use rotating pseudonym, not account identity |
| Issued-at timestamp | Milliseconds; reject >5 minutes future skew and age >24 hours |
| Max hops / hops | Max hops 1–8; current hop count 0..max hops |
| Payload length | Bounded; payload maximum 60,000 bytes |
| Payload | Typed application payload; schema and privacy policy must be versioned |
| Authentication tag | HMAC-SHA256, 32 bytes; requires a trusted shared key |

The current encoder also caps the complete packet at 65,536 bytes and rejects unsupported versions, malformed lengths, and trailing bytes.

## 3. Key and peer identity requirements

Before operational deployment, define an authenticated key agreement and peer-binding protocol. It must prevent an attacker from claiming another peer's token, substituting keys, or acknowledging another peer's message. Do not ship an app-wide hard-coded key or accept a key supplied by the packet sender.

The ACK primitive requires at least 256 bits of shared key material and authenticates the message ID and peer token. The transport must establish, authenticate, scope, rotate, and revoke that key; the current queue cannot prove that on its own.

## 4. Forwarding and deduplication

- Verify packet integrity and schema before parsing/dispatching the payload.
- Reject expired, future-dated, malformed, oversized, unsupported-version, and over-hop-limit packets.
- Apply a persistent seen-message cache keyed by message ID and bounded by retention/capacity.
- Forward only approved payload types, increment hops, preserve message ID, and never forward private data without an explicit protocol authorization.
- Use bounded queue depth, bounded retries, expiry, per-peer rate limiting, and backoff with jitter.
- Do not treat a local `SENT` queue status as recipient delivery; only authenticated receipt/ack can support the relevant state.

## 5. Radio behavior

BLE discovery currently uses a 15-second scan window policy. That is a discovery window, not proof of a data channel or message delivery. Implement and test a real GATT/advertising data path or another documented supported BLE transport, and implement Wi-Fi Direct separately with permission/lifecycle handling. Radio operations must gracefully handle disabled adapters, permission revocation, Android background limits, manufacturer differences, and process death.

Never broadcast precise coordinates, phone numbers, contact lists, voice-note bytes, or Medical ID data in BLE advertisements. Exchange sensitive payloads only after authenticated connection and explicit policy checks.

## 6. Delivery semantics

Track at least: QUEUED, ATTEMPTING, SENT_TO_PEER, ACKED, RETRY, FAILED, EXPIRED. Map these to SOS UI states only through a defined transport contract. A peer ACK confirms receipt by that peer only; it does not imply receipt by the intended emergency contact or dispatch service. Multi-hop forwarding must retain the original event ID and must not fabricate end-to-end acknowledgement.

## 7. Required interoperability tests

Test version mismatch, invalid MAC, wrong key, forged peer token, ACK replay, message replay, duplicate IDs, clock drift, expired packets, malformed lengths, max packet size, hop-limit boundaries, full queue, retries/exhaustion, interrupted radio connection, BLE permission revocation, Wi-Fi Direct group formation failure, background restrictions, and mixed-device interoperability. Measure battery consumption of the 15-second discovery policy on physical devices.
