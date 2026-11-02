# Phase 5: protected medical and contact data

**Specification only; no cryptographic implementation exists here.**

1. Define a data classification and threat model. Use Android Keystore-backed keys where available, with documented fallback and explicit recovery policy. Browser `crypto.subtle` is not the Android Keystore and `window.subtle` is not the Web Crypto API.
2. Persist versioned authenticated-encryption envelopes with fresh AES-GCM nonce per encryption and associated metadata. Test reopen after process death, restore, corruption, migration, rotation and key loss. Never generate a new master key on every read.
3. If a user PIN unlocks data, use a salted, rate-limited password KDF; avoid plaintext PIN storage/comparison. Length-dependent string comparisons are not constant-time authentication.
4. A standard QR camera cannot decrypt encrypted medical data without a compatible key. Require explicit sharing choice and a compatible responder workflow; do not expose sensitive details on a public screen by default.
5. Defer duress-triggered dispatch and panic-wipe claims until separate safety/legal/privacy review. Never promise physical protection.

**Acceptance gate:** independently reviewed key lifecycle, authenticated-encryption tests, PIN abuse tests, documented backup/deletion behavior and privacy review.
