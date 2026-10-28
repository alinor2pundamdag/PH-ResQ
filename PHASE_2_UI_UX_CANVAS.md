# Phase 2 — Emergency-First UI, Accessibility & Visual Identity

**Status:** Partial Compose UI and visual identity controls exist; no formal performance, TalkBack, contrast, touch-target, or physical-device accessibility pass is implied.

## Objective
Deliver a readable, low-friction emergency UI with the PH-ResQ visual identity, high-contrast accessibility, safe-area support, and battery-aware animation.

## 1. Visual identity
Preserve the requested:
- PH-ResQ / PH-ResQ Vanguard 2026 title.
- Infinite Philippine-flag color-looping title using yellow, blue, red, and white.
- Optional waving Philippine-flag background/canvas-like visual.
- Clean emergency-first layout with the most important actions immediately accessible.

Implement visuals using the selected Android UI framework or a documented native rendering component. Earlier DOM/canvas examples are conceptual only and must not be used as proof of native implementation.

## 2. Safety and usability rules
- Never place animation over SOS controls, consent actions, or status/error messages.
- Stop or reduce animation when backgrounded, low on battery, or when reduced-motion is enabled.
- Maintain legible text contrast in every phase of the flag animation; do not rely on color alone for status.
- Support Android safe areas/window insets, display cutouts, font scaling, landscape where supported, and screen-reader navigation.
- Provide high-contrast mode, large touch targets, TalkBack labels, haptic feedback controls, and reduced-motion behavior.
- SOS activation must use the specified two-second hold guard and clear countdown/cancellation feedback. Do not hide critical state behind decorative UI.
- Audio/siren behavior must respect stealth mode, safe-volume policy, and user settings.

## 3. Layout and interaction specification
- Startup and consent screens must be accessible and readable without animation.
- Dashboard must surface SOS, emergency contacts, local directory/search, offline map access, family check-in, and Medical ID without misleading “online” indicators when offline.
- Show explicit stale/unavailable labels for location, cached alerts, directory records, and map coverage.
- Provide visible pending/sent-to-handoff/delivered/acknowledged/failed status where relevant.
- Do not use color alone to distinguish statuses; combine icon/text/state descriptions.

## Current implementation boundary
A flag-title animation and reduce-motion control exist, along with several emergency flows. Their presence is not evidence of complete screen coverage, accessible semantics, or device acceptance. The SOS path must remain unobstructed in all states.

## Acceptance criteria
- Accessibility audit covers TalkBack, text scaling, high contrast, reduced motion, and touch targets.
- Emergency controls remain usable at supported screen sizes and with animation disabled.
- Animation does not obscure text or prevent emergency action; CPU/battery behavior is measured on target devices.
- UI correctly reflects offline mode, stale data, permission denial, and SOS delivery limitations.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-004, T-023.
