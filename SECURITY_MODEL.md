# PH-ResQ Security Model and Threat Register

**Status:** Design baseline with partial security utilities. This document is not a penetration-test report or a certification.
**Related:** `ARCHITECTURE_DECISIONS.md`, `DATABASE_SPEC.md`, `MESH_PROTOCOL.md`, `DATA_SOURCES_AND_RISKS.md`.

## 1. Assets

- Emergency contact names, phone numbers, and relationship labels.
- SOS drafts, recipient selections, precise location and location history.
- Medical ID fields and generated QR payloads.
- Voice notes and attachments.
- PIN-vault plaintext and derived key material.
- Mesh messages, peer tokens, ACK keys, queue metadata, and replay cache.
- Consent state, search history, bookmarks, family check-ins, and pending actions.
- Integrity manifests, dataset source metadata, signing material, and release artifacts.

## 2. Adversaries and trust boundaries

Consider a lost or stolen locked device; a person with temporary physical access; a malicious or compromised app; a hostile nearby peer; replay/injection of radio packets; tampered or forged imported JSON; a compromised network/source; accidental disclosure via notifications, screenshots, clipboard, QR, logs, or Android share sheet; and process termination or power loss during critical writes.

Trust boundaries include Android OS and Keystore; PH-ResQ process; local database/files; user-selected external apps; imported datasets; public advisories; and any future relay/server or peer transport. External messaging apps and recipients are not controlled by PH-ResQ.

## 3. Security controls and current status

| Control | Intended protection | Current implementation / limitation |
|---|---|---|
| AES-GCM field encryption | Protect selected sensitive database fields at rest | `EncryptedFieldCipher` exists; key lifecycle, rotation, recovery, backup behavior, and complete field coverage require audit |
| PIN-protected vault | Require a PIN to reveal selected contact data | PBKDF2-derived key + AES-GCM and temporary lockout exist; not a duress vault, not resistant to a compromised/unlocked device, and no recovery flow |
| Android Keystore | Device-bound key protection | Used by field cipher where supported; StrongBox availability is optional and device-dependent |
| HMAC-SHA256 mesh envelope | Detect modification by a party lacking shared key | Codec utility exists; authenticated peer key establishment and radio transport are not implemented |
| HMAC ACK | Authenticate an ACK under an already-established shared key | Queue primitive exists; transport must safely establish and bind that key to the peer |
| SHA-256 integrity check | Detect mismatch against trusted expected digest | Verifier/cache primitives exist; a checksum beside mutable content does not authenticate it or repair it |
| Consent gate | Prevent protected feature use before acceptance | Versioned consent flow exists; verify all navigation/lifecycle paths |
| Foreground SOS status | Visible handoff status with stop action | Service exists; notification permission and physical-device lifecycle behavior need validation |
| JSON validation | Reject malformed data | Structural validation exists; source metadata is not a signature |
| QR privacy | Limit accidental Medical ID exposure | QR is not encrypted by default; user must select fields and control display/share |

## 4. Required controls

- Minimize collection and do not request permissions before the related feature needs them.
- Never write PINs, derived keys, plaintext vault data, SOS content, full phone numbers, precise coordinates, voice-note contents, or cryptographic keys to logs.
- Use unique random AES-GCM nonces per key and authenticated encryption; test nonce generation and tamper rejection.
- Apply rate limits and lockouts to PIN attempts, but document that local lockout can be bypassed by app-data reset or a compromised device unless additional OS-backed controls prevent it.
- Define device-bound key loss, reinstall, backup/restore, rotation, and revocation before production release.
- Keep notifications minimal: never expose recipient numbers, exact coordinates, medical details, or full distress message on lock screen by default.
- Treat imported JSON and remote advisory content as untrusted data. Escape text in UI and validate all fields.
- Establish mesh peer identity and key agreement before accepting ACKs or forwarding private payloads. A hard-coded/shared app-wide key is not acceptable.
- Protect release signing keys outside source control; scan repository history and CI logs for secrets.
- Publish privacy notice and retention/deletion rules that match actual behavior.

## 5. Mesh threat handling

Reject unsupported protocol versions, oversized packets, invalid timestamps, expired TTL, excessive hop counts, malformed lengths, trailing bytes, bad MACs, duplicate IDs, and ACKs whose MAC does not match the expected peer-bound key. Limit queue growth, per-peer rate, retry count, and retention. Do not include precise location or Medical ID data in BLE advertisements.

HMAC authenticity is only as strong as key distribution. Until authenticated key establishment exists, the current codec/queue must be treated as testable primitives, not a secure operational mesh.

## 6. Residual risks / prohibited claims

- No absolute secrecy, "zero indication", "un-bypassable vault", or guaranteed delivery claim.
- PIN vault is not duress mode and cannot hide all OS/network/forensic traces.
- QR screenshots, screen capture, and a malicious recipient can expose selected fields.
- Android device compromise, rooted OS, malicious accessibility service, or unlocked-device access may defeat app-level controls.
- SHA-256 alone does not prove source authenticity.
- Imported metadata may be forged.
- No complete privacy/legal review, penetration test, or physical-device security acceptance is evidenced.
