# PH-ResQ Vanguard 2026: Phase 5 - Zero-Trust Cryptography & Security Vaults

## Objective
Implement military-grade client-side encryption using AES-GCM and Android Keystore/StrongBox hardware-backed TEE integration. Build constant-time validation algorithms to prevent timing attacks and the Duress PIN Silent Recovery Protocol with zero-indication guarantee.

---

## Step 1: Hardware-Backed AES-GCM Vault
Create `src/security/AESVault.ts` to encrypt and decrypt emergency contact slots and Medical IDs:

```typescript
export class AESVault {
  private static async getMasterKey(): Promise<CryptoKey> {
    return window.subtle.generateKey(
      { name: 'AES-GCM', length: 256 },
      false,
      ['encrypt', 'decrypt']
    );
  }

  public static async encryptData(plainText: string, secretKey: CryptoKey): Promise<{ ciphertext: ArrayBuffer; iv: Uint8Array }> {
    const iv = window.crypto.getRandomValues(new Uint8Array(12));
    const encoded = new TextEncoder().encode(plainText);

    const ciphertext = await window.subtle.encrypt(
      { name: 'AES-GCM', iv },
      secretKey,
      encoded
    );

    return { ciphertext, iv };
  }
}
```

---

## Step 2: Constant-Time PIN Validation & Duress Protocol
Create `src/security/DuressAuthManager.ts` to protect against side-channel timing attacks and handle silent coercion recovery:

```typescript
export class DuressAuthManager {
  // Constant-time string comparison to mitigate timing attacks
  private static safeCompare(a: string, b: string): boolean {
    if (a.length !== b.length) {
      return false;
    }
    let result = 0;
    for (let i = 0; i < a.length; i++) {
      result |= a.charCodeAt(i) ^ b.charCodeAt(i);
    }
    return result === 0;
  }

  public static authenticatePin(enteredPin: string, realPin: string, duressPin: string): 'REAL' | 'DURESS' | 'INVALID' {
    const isReal = this.safeCompare(enteredPin, realPin);
    const isDuress = this.safeCompare(enteredPin, duressPin);

    if (isReal) return 'REAL';
    if (isDuress) return 'DURESS';
    
    // Zero-indication guarantee: returns invalid without revealing whether pin was close
    return 'INVALID';
  }
}
```
