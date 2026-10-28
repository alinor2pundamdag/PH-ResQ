# PH-ResQ SOS State Machine

**Status:** Normative behavior specification. The Kotlin enum and transition guard are a partial implementation; persistent transition orchestration, SMS delivery receipts, and physical-device acceptance remain incomplete.
**Related:** `MASTER_PLAN.md`, `ARCHITECTURE_DECISIONS.md`, `FEATURE_TRACEABILITY.md`, `TEST_AND_ACCEPTANCE_PLAN.md`.

## 1. Design rules

1. A user action creates one durable SOS event with a unique event ID before any external handoff is attempted.
2. UI state must reflect the persisted event state, not merely a button callback.
3. "Handed off" means another app/platform surface was opened; it is not proof that the user pressed Send, that the carrier accepted the SMS, or that a recipient received it.
4. "Delivered" is allowed only when a trusted transport receipt supports that exact claim. "Acknowledged" requires a recipient-side acknowledgement linked to the event ID.
5. A process restart must recover pending state without silently losing the event or blindly creating duplicate sends.
6. Cancellation is only possible before the relevant transport becomes irreversible. After handoff, explain that the user may need to cancel in the messaging app or contact recipients directly.

## 2. States and meaning

| State | Meaning | User-facing wording |
|---|---|---|
| IDLE | No active event | Ready |
| QUEUED | Event durably stored, awaiting an allowed send/handoff path | SOS saved; waiting |
| SENDING | A supported transport attempt is in progress | Attempting to send |
| HANDED_OFF | Messaging app or OS handoff opened successfully | Opened messaging app; delivery unconfirmed |
| DELIVERED | Transport supplied a verified delivery receipt | Delivered (receipt available) |
| ACKNOWLEDGED | A recipient or authorized receiving service acknowledged the event | Acknowledged |
| RETRY_PENDING | A retry is permitted by bounded policy | Retry pending |
| CANCELLED | User cancelled before irreversible dispatch | Cancelled |
| FAILED | Attempt cannot proceed without recovery or user action | Not sent / failed; explain reason |

The current app uses `SosState` and `SosStateMachine` for a subset of legal transitions. It does not yet provide a complete transport-backed delivery-receipt pipeline. Do not display DELIVERED unless that pipeline exists and is tested.

## 3. Legal transitions

- IDLE → QUEUED or CANCELLED
- QUEUED → SENDING, HANDED_OFF, CANCELLED, or FAILED
- SENDING → HANDED_OFF, DELIVERED, RETRY_PENDING, FAILED, or CANCELLED
- HANDED_OFF → DELIVERED, ACKNOWLEDGED, or FAILED only when evidence supports the transition; handoff alone cannot advance to delivery
- DELIVERED → ACKNOWLEDGED or FAILED
- RETRY_PENDING → SENDING, CANCELLED, or FAILED
- FAILED → QUEUED only after explicit retry/recovery
- ACKNOWLEDGED and CANCELLED are terminal for that event; a new emergency creates a new event ID

Every attempted transition must be checked against the transition table and persisted transactionally with timestamp and non-sensitive reason code. Reject unknown transitions and log no message body, phone number, PIN, or precise location.

## 4. SOS payload and privacy

The draft may include a user-approved distress message, available location coordinates, capture time, accuracy, and selected emergency contacts. Mark location missing/stale rather than fabricating it. Voice notes are optional and require explicit user action. Never add Medical ID fields or share a vault payload without explicit selection and disclosure.

## 5. Timing, retry and idempotency

- Require the specified two-second hold or an accessible equivalent that still guards against accidental activation.
- Persist before dispatch; generate an event ID once and reuse it for retries.
- Apply bounded retries with exponential backoff and jitter for transports that support automatic retry. Do not automatically repeat a user-controlled messaging-app handoff without asking.
- Distinguish retrying a failed local operation from re-sending an SMS that may already have been accepted.
- Record attempt count, last attempt time, next eligible retry, transport, and safe error code.

## 6. Failure handling

| Failure | Required behavior |
|---|---|
| Permission denied / no SIM / no messaging app | Keep event recoverable; show clear next action |
| No GPS fix | Allow SOS without coordinates; say location is unavailable |
| Stale GPS | Include timestamp/accuracy and mark as stale |
| Process death during write | Transaction rollback or recoverable prior state |
| Process death after handoff | Restore HANDED_OFF; never infer delivery |
| Duplicate button taps | Same event/operation guard; no duplicate event |
| Retry limit reached | FAILED with explicit manual retry/recovery |
| Notification permission denied | Explain notification limitation; do not imply the SOS itself was delivered |
| Recipient does not acknowledge | Remain at the strongest evidence-backed state |

## 7. Acceptance evidence

Required tests: all legal/illegal transitions; duplicate taps; process death at each boundary; permission/no-SIM cases; stale/missing location; retry bounds; receipt authenticity; and truthful UI copy. Physical-device SMS behavior must be recorded by Android version, messaging app, SIM/carrier configuration, and test date. See `TEST_AND_ACCEPTANCE_PLAN.md`.
