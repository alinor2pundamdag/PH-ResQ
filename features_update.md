# PH-ResQ: proposed feature backlog

**Nothing in this list is implemented in this repository.** See `README.md` for current status and `MASTER_PLAN.md` for release gates.

| Priority | Proposed capability | Verification needed before any public claim |
| --- | --- | --- |
| P0 | Offline emergency guidance and accessible SOS screen | Device tests without internet, permissions and GPS |
| P0 | User-managed emergency contacts; SMS/call attempt and manual fallback | Delivery-state and failure-path tests on real SIMs/devices |
| P0 | Licensed, dated local hotline/directory snapshot | Source, coverage, accuracy and update/rollback audit |
| P0 | Privacy choices and protected local medical/contact data | Legal review, key recovery and deletion/security tests |
| P1 | Offline maps and location fallback | Footprint, accuracy, freshness and low-memory tests |
| P1 | Weather/evacuation cache and check-in sync | Data provenance, timestamp and offline queue tests |
| P2 | BLE/Wi-Fi Direct proximity messaging | Native compatibility, permissions, battery and field trials |
| P2 | Duress mode, responder QR and stealth signaling | Independent safety/privacy threat assessment |
| P2 | Audio notes and visual animation | Accessibility, device policy, storage and battery tests |

Do not advertise real-time evacuation capacity while offline, guaranteed message delivery, cell-broadcast interception or automatic hotline fallback without demonstrated platform support and vetted data.
