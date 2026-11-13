# PH-ResQ SOS State Machine

**Status:** Normative behavior specification. Current source persists an encrypted draft/recipient/location snapshot before messaging handoff, assigns a stable UUID per SOS event, records QUEUED/SENDING/HANDED_OFF/FAILED transitions, and blocks generic DELIVERED/ACKNOWLEDGED writes without trusted receipt evidence. Event-scoped recovery, trusted receipts, and physical-device acceptance remain incomplete.
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

The current app uses `SosState`, `SosStateMachine`, and schema-v8 `event_id` to persist an event snapshot before external handoff. On startup, `reconcileInterruptedSosHandoffs()` examines the latest state of every event: QUEUED becomes INTERRUPTED_NOT_SENT, while SENDING becomes HANDOFF_UNCONFIRMED. It preserves the original encrypted snapshot, is idempotent, runs off the UI thread, and never auto-reopens or resends the SMS composer. Generic transition writes cannot set DELIVERED or ACKNOWLEDGED. The app has no transport-backed delivery-receipt pipeline; do not display either state unless a trusted receipt/acknowledgement adapter is implemented and tested.

## 3. Legal transitions

- IDLE → QUEUED or CANCELLED
- QUEUED → SENDING, HANDED_OFF, CANCELLED, or FAILED
- SENDING → HANDED_OFF, DELIVERED, RETRY_PENDING, FAILED, or CANCELLED
- QUEUED → INTERRUPTED_NOT_SENT when startup finds a queued event that never reached SENDING; no external messaging handoff was started.
- SENDING → HANDOFF_UNCONFIRMED when startup finds a process-interrupted external handoff; this is terminal and requires user review. The state is intentionally ambiguous because the messaging app may have opened before process death.
- INTERRUPTED_NOT_SENT and HANDOFF_UNCONFIRMED are terminal; the user must initiate a new explicit SOS event if needed.
- HANDED_OFF → FAILED in the current implementation. A future trusted transport may add receipt-backed delivery/acknowledgement transitions only through a separate evidence-verifying API.
- DELIVERED and ACKNOWLEDGED are retained as domain states but are unreachable through the generic current transition API.
- RETRY_PENDING → SENDING, CANCELLED, or FAILED
- FAILED → QUEUED only after explicit retry/recovery
- ACKNOWLEDGED and CANCELLED are terminal for that event; a new emergency creates a new event ID

Every attempted transition must be checked against the transition table and persisted transactionally with timestamp and non-sensitive reason code. Current schema v8 associates transitions with a stable UUID event_id; legacy rows receive independent IDs during migration because their historical grouping cannot be inferred safely. Reject unknown transitions and log no message body, phone number, PIN, or precise location.

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
| Process death while QUEUED | Append INTERRUPTED_NOT_SENT; explain no messaging app was opened and do not auto-send |
| Process death while SENDING | Append HANDOFF_UNCONFIRMED, preserve the event snapshot, and never auto-resend; ask the user to check the messaging app | 
| Process death after handoff | Restore HANDED_OFF; never infer delivery |
| Duplicate button taps | Same event/operation guard; no duplicate event |
| Retry limit reached | FAILED with explicit manual retry/recovery |
| Notification permission denied | Explain notification limitation; do not imply the SOS itself was delivered |
| Recipient does not acknowledge | Remain at the strongest evidence-backed state |

## 7. Acceptance evidence

Required tests: all legal/illegal transitions; duplicate taps; process death at each boundary; permission/no-SIM cases; stale/missing location; retry bounds; receipt authenticity; and truthful UI copy. Physical-device SMS behavior must be recorded by Android version, messaging app, SIM/carrier configuration, and test date. See `TEST_AND_ACCEPTANCE_PLAN.md`.
