# Phase 2: accessible crisis interface

**Specification only; no UI code exists here.**

1. Put emergency action and manual-call guidance ahead of decorative elements. Provide clear SOS confirmation/cancel and visible status, with an alternative to press-and-hold for motor-limited users.
2. Support dynamic text, high contrast, screen readers, focus order, touch target sizing, daylight use and reduced motion. Verify color contrast and orientation/insets on target devices.
3. Any animated flag/header must be optional, stop on background/unmount, honor reduced-motion and low-power settings and not delay critical controls. Treat canvas context failure as normal; static header fallback required.

**Acceptance gate:** manual accessibility review and device tests on low-end Android, with animation on/off, battery saver, large text, TalkBack, rotation and interrupted activity lifecycle.
