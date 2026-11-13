# PH-ResQ Settings, Device Capabilities & Integration Contracts

**Status:** Target contract; settings and capability checks are currently fragmented and only partly wired.
**Related:** `ANDROID_COMPATIBILITY_AND_ADAPTIVE_RUNTIME.md`, `PERSISTENCE_RECOVERY_ARCHITECTURE.md`, `SYSTEM_ARCHITECTURE.md`, `FEATURE_TRACEABILITY.md`.

## 1. Three authorities

- **SettingsRepository:** stores user intent and preference (for example reduce motion, high contrast, location cadence preference, audio gain, privacy choices).
- **DeviceCapabilityProvider:** reports actual OS/hardware/permission/system state.
- **FeaturePolicyCoordinator:** combines the two and determines effective behavior for a feature. It may constrain a preference for safety/platform compliance, but must not silently rewrite the user's preference.

The UI observes all three and renders the effective state plus a reason. A user setting cannot grant a missing Android permission or create missing hardware. A capability must never be inferred from a setting toggle.

## 2. Contract shape

Each capability exposes:
`id`, `state`, `apiLevel`, `permissionState`, `reasonCode`, `checkedAt`, `fallbackId`, and optional `diagnosticReference`.

Each feature policy exposes:
`featureId`, `userPreference`, `effectiveMode`, `availability`, `reason`, `lastTransitionAt`, and `requiredPermission`.

All UI-facing state is immutable and lifecycle-aware. Hardware/permission events refresh the capability snapshot. Repeated refreshes must be idempotent and must not start duplicate scans/services.

## 3. Settings ownership

| Setting | Persistent owner | Capability/policy dependency | UI contract |
|---|---|---|---|
| Consent/policy version | ConsentStore | None; must be accepted before protected features | Show current version, accepted/declined/revoked |
| Reduce motion | SettingsRepository | System animator scale/accessibility settings | Effective motion mode and reason |
| High contrast | SettingsRepository | Display/theme | Persisted preference; test contrast |
| Location mode/cadence | SettingsRepository | Location providers, permission, battery saver, lifecycle | Show current/last-known/stale/denied; no hidden tracking |
| Notifications | OS permission/channel + app preference | API level and user OS settings | Explain disabled channel/permission; in-app fallback |
| BLE discovery | User action/preference | BLE feature, adapter, permission, app lifecycle | Available/scanning/disabled/denied/unsupported; discovery only |
| Wi-Fi Direct | User action/preference | Hardware, permission, platform API, peer support | Not implemented until real transport; never show connected from discovery |
| Vault lock | Vault metadata/Keystore | Key availability and PIN state | Locked/unlocked/unrecoverable distinctly |
| Voice-note gain | SettingsRepository | Audio output/system volume | Clamp gain; never override system volume silently |
| Offline datasets | Dataset repository | Storage, manifest signature, freshness, license | Version/source/freshness/coverage; distinguish imported from authenticated |
| Map downloads | MapTileRepository | Storage/network/license/renderer | Cached coverage vs actual renderable offline map |
| SOS status | SOS repository | Messaging app/SIM/notification permission | Queued/handoff/receipt/ack states independently |
| Family check-in | Checkin repository/outbox | Network/authenticated endpoint (future) | Local saved/pending/synced/failed; no fake sync |

## 4. Integration lifecycle

1. App starts; load persisted settings and database schema state.
2. Build capability snapshot from Android APIs and current permission/system state.
3. Derive effective feature policies; do not mutate saved preferences.
4. UI renders status and safe fallbacks.
5. On permission result, radio change, battery mode, configuration change or resume, refresh only affected capabilities.
6. Persist critical domain state before platform operations.
7. Platform adapter returns typed outcome with evidence.
8. Domain coordinator persists outcome and emits UI state.
9. On process restart, rebuild from durable records and fresh capability checks.

## 5. Required consistency rules

- Settings never directly call hardware APIs from Compose UI.
- Hardware adapters never modify user settings.
- Feature policy never fabricates a successful result when capability is unavailable.
- A UI button is disabled only with an explanatory reason and alternative where possible.
- A permission denial must not crash the app or erase user data.
- No capability refresh creates duplicate background work.
- Diagnostics are redacted and never contain secrets, phone numbers, precise location, PINs, voice contents or tokens.
