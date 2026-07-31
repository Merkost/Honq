# Setup Flow and Readiness UI/UX Design

## Status

Approved design for implementation.

## Goal

Make the path from first launch to starting practice feel fast, understandable, and dependable. The work covers the onboarding state/licence flow, the home setup sheet, the readiness card, and the loading, empty, unavailable, and offline states shared by those surfaces.

## Context

The app supports Australian state and territory driver-licence study. Users choose a state and licence type before studying. The current implementation already provides adaptive readiness-card headers, a scrollable setup sheet, radio semantics, and state/licence choice cards. The next pass should improve hierarchy and feedback without changing the underlying question-bank or persistence contracts.

## Recommended approach

Use one cohesive setup model across onboarding and the home sheet:

1. Make the two-step onboarding flow visibly progressive and give the user a compact setup summary before starting.
2. Make the home sheet a clear “change setup” surface with state selection, licence availability, and explicit sync feedback.
3. Keep the readiness card information-dense but scannable by separating context, status, score, and the study action.
4. Use the same copy, selection treatment, loading behavior, and disabled-state language across all three surfaces.

## Experience design

### Onboarding

- Retain the two-step flow: state first, licence type second.
- Keep `STEP 1 OF 2` and `STEP 2 OF 2` visible near the back action.
- Give every choice a large, full-row target with radio semantics and a strong selected treatment.
- Preserve state-code badges and licence icons as quick visual anchors.
- Show a compact “Your setup” summary once both selections are known, using the selected state and licence name/code.
- Keep the primary action anchored near the bottom of the content area and make its enabled/disabled/loading state obvious.
- If question-bank loading fails, keep the selected choices visible and offer an inline retry with a short explanation.

### Home setup sheet

- Present the sheet as “Choose your study setup” with one short explanatory subtitle.
- Keep state choices compact and scannable in a consistent grid.
- Present licence types as full-width rows with icon, name, short code, availability, and a radio/check indicator.
- While a new state is syncing, show an updating label and avoid presenting stale licence availability as current.
- For states without an in-app bank, explain that the option is unavailable for this state and retain the existing external-practice-link path.
- Keep the sheet vertically scrollable and ensure all choices remain reachable on small screens and with larger font settings.

### Readiness card

- Preserve the card’s score and gauge as the visual focal point.
- Keep the selected setup in a compact context row, such as `NSW · C · Car`, with a clear change affordance.
- Keep the readiness status separate from the context so `KEEP STUDYING`, `ON TRACK`, or `READY` cannot collide with the setup label.
- Maintain the adaptive stacked header on narrow widths and prevent status labels from wrapping.
- Keep the score, pass-distance message, and answered/accuracy caption in a stable vertical hierarchy.
- Make the complete card or its explicit study action easy to activate without creating nested or ambiguous tap targets.

## State and feedback behavior

- Selection remains controlled by the existing presentation state and callbacks.
- A state change invalidates visible licence availability until the new state’s data is loaded; the UI communicates this as an update rather than showing mismatched content.
- A successful selection returns the user to the normal readiness/home state.
- An empty result shows the existing no-question explanation and external practice action where available.
- A recoverable sync error preserves the user’s selections and exposes retry feedback.
- Existing content, persistence, analytics, and external-link contracts remain unchanged unless a small presentation-only seam is required to expose the feedback state.

## Accessibility and motion

- Keep state and licence choices exposed as radio-button semantics with selected and disabled states.
- Maintain minimum touch targets for every interactive choice and the setup change affordance.
- Use text labels in addition to color, icons, and borders for availability and selection.
- Keep descriptions readable with max lines and ellipsis only where the complete meaning remains available from the primary label/code.
- Reuse existing Honq theme colors, spacing, shapes, and motion tokens.
- Respect reduced-motion behavior already established by the project; selection feedback must remain understandable without animation.

## Scope boundaries

Included:

- Shared onboarding and home setup UI.
- Readiness-card hierarchy and responsive layout.
- User-facing strings for setup, loading, retry, unavailable, and empty states.
- Focused pure-logic layout/selection tests and manual emulator verification.

Not included:

- New question-bank formats or imports.
- Changes to Firestore schemas, repository contracts, or persistence models.
- A redesign of unrelated home sections, practice screens, paywall, or statistics.
- Replacing the existing Compose Multiplatform navigation or theme system.

## Implementation shape

- Keep state and callbacks hoisted through the existing onboarding and home containers.
- Extract small stateless composables for setup summaries, selection rows, feedback banners, and readiness-card header/content groups where that makes behavior easier to test and review.
- Add or extend focused common tests for responsive header layout and state-dependent choice presentation.
- Preserve unrelated working-tree changes, especially question-bank and import files.

## Verification

- Run the focused common tests for responsive readiness-card behavior.
- Run shared Android compilation and unit tests.
- Run iOS simulator compilation for common UI compatibility.
- Assemble the Android debug APK.
- Manually verify first-launch onboarding, setup changes from home, syncing, unavailable states, empty states, and narrow-width readiness-card layout on the emulator.
- Run whitespace/diff checks and report any broader test suite that cannot complete separately from passing gates.
