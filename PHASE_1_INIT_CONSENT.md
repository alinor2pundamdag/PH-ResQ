# PH-ResQ Phase 1 — Android Foundation and Consent

## Runtime
Native Android Kotlin + Jetpack Compose. Consent is enforced by `ConsentStore` before the main screen is composed.

## First-launch consent contract
- Display the current policy notice on first launch and whenever `CURRENT_POLICY_VERSION` changes.
- Store the exact policy version and accepted timestamp only after the user explicitly taps Accept.
- Decline must not grant access to the main screen; it exits the current activity.
- The notice body must be vertically scrollable independently from its action area.
- Accept and Decline controls remain visible and reachable on short screens, landscape, gesture navigation, display cutouts, and large text settings.
- Do not require the user to scroll to the bottom before the controls appear; do not hide either choice behind the scroll container.
- Use safe drawing insets so content and touch targets do not overlap status/navigation bars or cutouts.
- Changing the notice content materially requires a policy-version bump, which invalidates older acceptance and presents the updated notice again.

## Current implementation
The consent screen uses a weighted vertical-scroll body and a fixed action card with Accept and Decline buttons outside the scroll container. It applies `safeContentPadding` so system bars, display cutouts, and gesture-safe regions are respected. The current policy is `2026-11-02-v2`. Decline exits; acceptance is synchronously committed before the main screen is shown.

## Required validation
- ConsentStore tests: no acceptance by default, version mismatch, persistence, and revoke/decline.
- UI acceptance on compact phone, large font/display size, landscape, 3-button navigation, gesture navigation, and display cutout.
- TalkBack announces title, notice sections, and both actions in logical order.
- Accept is not triggered by scroll or accidental touch; decline never reaches the main screen.
- Reinstall/clear-data behavior is documented; do not claim consent survives app data removal.