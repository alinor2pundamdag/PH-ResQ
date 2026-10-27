# Phase 5 — Encrypted Vault, PIN Security & Privacy

**Status:** Security specification only; no cryptographic implementation or security audit is implied.

## Objective
Protect emergency contacts and sensitive Medical ID data at rest, define the key lifecycle, and preserve optional duress behavior without making impossible secrecy guarantees.

## 1. Encryption and key management
- Use authenticated encryption such as AES-GCM for sensitive vault records.
- Use Android Keystore-backed keys where feasible; StrongBox is optional and device-dependent. Document the fallback on unsupported devices.
- Never reuse a GCM nonce with the same key. Use a documented nonce-generation strategy and test uniqueness.
- Specify key generation, storage, rotation, revocation, backup, restore, reinstall, device migration, lockout, and recovery before implementation.
- If a PIN contributes to key derivation, use a suitable password-based KDF with a per-vault salt and calibrated cost. Apply rate limiting and secure failure handling; a PIN is not itself an encryption key.
- Keep keys and plaintext out of logs, crash reports, analytics, clipboard, and unencrypted backups.

## 2. Vault data scope
Protect emergency contacts and selected Medical ID fields. Define which fields are encrypted, which metadata is exposed, and when plaintext is briefly present in memory. Minimize data retained outside the vault.

## 3. Optional duress PIN and dummy vault
Preserve the requested secondary duress PIN, clean dummy vault, and discreet distress-signal concept as an opt-in, threat-modelled feature.

Do not claim a “zero-indication guarantee.” Timing, UI behavior, notification state, network activity, forensic access, and device/OS behavior can reveal activity. Do not promise absolute physical protection. Explain limitations during setup and test permission denial, no network, and failed dispatch.

The distress state must not be reported as sent unless the transport confirms the relevant state. Avoid revealing sensitive information in logs or error messages.

## 4. Rolling privacy tokens
Define cryptographic token generation, rotation interval, clock-drift handling, collision behavior, replay protection, and how peers authenticate without exposing a permanent identifier. Test token unlinkability to the extent feasible and document residual risks.

## 5. Offline Medical ID QR
The QR feature must remain available offline, but the QR is not inherently encrypted: any compatible camera/scanner may read its contents. Include only user-selected minimal fields, show an explicit reveal/share action, and warn before display. Do not imply that an arbitrary camera can decrypt a vault ciphertext.

## Acceptance criteria
- Encryption/decryption round-trip and tamper detection pass.
- Nonce uniqueness, key lifecycle, PIN rate limiting, backup/reinstall, and recovery behavior are tested and documented.
- No sensitive plaintext or secrets appear in logs.
- Duress-mode threat model and limitations are disclosed; no absolute secrecy claim.
- QR payload contains only the user-selected fields and the disclosure warning is accessible.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-012, T-013, T-022.
