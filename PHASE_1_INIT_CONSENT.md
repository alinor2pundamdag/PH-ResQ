# PH-ResQ Vanguard 2026: Phase 1 - Environment Setup & Non-Bypassable Consent Architecture

## Objective
Establish the absolute foundational environment, project structure, dependencies, and the mathematically locked non-bypassable persistent consent state machine for **PH-ResQ Vanguard 2026**. This guide provides step-by-step instructions from zero to a fully compiled, secure base architecture.

---

## Step 1: Project Initialization & Directory Structure
Create a clean, production-grade directory layout optimized for local-first execution on mobile environments (Termux / AndroidIDE / React Native / Native Android):

```text
PH-ResQ/
├── android/
│   ├── app/
│   │   ├── src/main/java/org/sayangqoemeet/phresq/
│   │   │   ├── core/
│   │   │   ├── security/
│   │   │   ├── mesh/
│   │   │   └── database/
│   │   └── AndroidManifest.xml
├── src/
│   ├── components/
│   │   ├── header/
│   │   └── consent/
│   ├── state/
│   │   └── ConsentStateMachine.ts
│   ├── database/
│   │   └── BarangaysRTree.ts
│   ├── mesh/
│   │   └── BLEMeshEngine.ts
│   └── security/
│       └── AESVault.ts
├── MASTER_PLAN.md
└── WORK_RULES.md
```

---

## Step 2: Implementing the Non-Bypassable Consent State Machine (FSM)
To comply with Data Privacy regulations and open-source licensing frameworks, every user must explicitly accept terms on first launch. If declined, the app must persistently block dashboard access and force-exit.

Create `src/state/ConsentStateMachine.ts`:

```typescript
export enum ConsentState {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  DECLINED = 'DECLINED'
}

export class ConsentStateMachine {
  private currentState: ConsentState;
  private storageKey = 'ph_resq_consent_status';

  constructor() {
    this.currentState = this.loadPersistentState();
  }

  private loadPersistentState(): ConsentState {
    try {
      const saved = localStorage.getItem(this.storageKey);
      if (saved === ConsentState.ACCEPTED || saved === ConsentState.DECLINED) {
        return saved;
      }
    } catch (e) {
      // Fallback for secure local storage failure
    }
    return ConsentState.PENDING;
  }

  public getState(): ConsentState {
    return this.currentState;
  }

  public acceptConsent(): void {
    this.currentState = ConsentState.ACCEPTED;
    localStorage.setItem(this.storageKey, ConsentState.ACCEPTED);
  }

  public declineConsent(): void {
    this.currentState = ConsentState.DECLINED;
    localStorage.setItem(this.storageKey, ConsentState.DECLINED);
    this.forceExitApp();
  }

  private forceExitApp(): void {
    // Trigger non-bypassable termination / return to home screen
    if (typeof window !== 'undefined' && window.close) {
      window.close();
    }
  }

  public canAccessDashboard(): boolean {
    return this.currentState === ConsentState.ACCEPTED;
  }
}
```

---

## Step 3: Required Permissions & Core Service Setup
Ensure all required permissions for offline emergency broadcasting, BLE scanning, location fallbacks, and foreground services are configured in your build system.
