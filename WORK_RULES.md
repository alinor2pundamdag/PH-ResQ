# PH-ResQ: work and safety rules

## Repository changes
Do not create, edit, commit or push repository files without explicit authorization from the owner. Audit-only requests are read-only. Keep changes reviewable and report what was tested and what remains unverified.

## Safety and evidence
- Never claim zero bugs, guaranteed rescue, guaranteed SMS delivery, exact BLE distance or legal compliance without evidence. Test graceful failure for denied permissions, unavailable GPS, dead battery, no cell coverage, corrupt storage and stale data.
- Treat this repository as a proposal until a working app and repeatable device tests exist. Do not ship illustrative snippets as production code.
- Preserve access to essential emergency information where possible without unnecessary consent. Obtain separate, specific consent for sensitive processing; offer withdrawal and deletion flows. Review privacy and emergency-access decisions with Philippine legal/privacy specialists.
- Never imply Apache 2.0 acceptance is required to use an app: the license governs distribution of repository contents, not an end-user privacy agreement.
- Minimize sensitive data; encrypt at rest with platform-managed keys where appropriate, authenticate peer traffic, specify retention/deletion, and avoid plaintext medical data in QR codes without explicit informed consent.
- Use explicit user confirmation for sending SOS and for any change to alarm volume. No stealth transmission or duress-triggered dispatch until safety, legal and false-trigger risks are reviewed.
- Validate hotline and evacuation data against authoritative sources, date each record and display stale/unverified status. Do not silently redirect calls to unverified numbers.
- Prefer a simple dependable emergency path over animation or experimental mesh networking; respect reduced-motion settings and battery limits.
