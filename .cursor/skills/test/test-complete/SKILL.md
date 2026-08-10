---
name: test-complete
description: Run all written unit/integration/E2E tests and perform an emulator walkthrough checklist of the app. Use when asking to test everything, full test pass, or emulator go-through. Requires emulator or physical device for connected tests and walkthrough.
---

# Complete Test Pass

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to run the full test pass and an emulator walkthrough — an emulator or physical device is required for connected/`androidTest` and the walkthrough (or we proceed with the device already attached).

If no device is attached: say so after the banner, run JVM `test` where possible, mark connected/`androidTest` and walkthrough as blocked — do not claim they passed.

Follow `.cursor/rules/11-testing.mdc`.

## Step 1 — Run existing automated suites

From project root (Gradle):

```bash
./gradlew test
./gradlew connectedDebugAndroidTest
```

On Windows PowerShell, use `.\gradlew.bat` equivalents.

- Report pass/fail per module
- Integration tests usually live under `src/test` (covered by `test`); use `connectedDebugAndroidTest` for E2E / androidTest integration
- If `connectedDebugAndroidTest` cannot run (no device), note blocker and continue with JVM results

## Step 2 — Align with test skills

- Gaps in UseCase/ViewModel/Flow-with-fakes → suggest `test-unit`
- Gaps in Room/MockWebServer multi-layer wiring → suggest `test-integration`
- Critical UX / device flow gaps → suggest `test-e2e`
- Honor `writeTestsWithFeatures` in `.cursor/project-settings.json`

## Step 3 — Emulator walkthrough checklist

On an emulator (or device) matching minSdk / target as practical:

- [ ] Cold start → Entrance (or start destination) loads
- [ ] Primary nav path(s) reachable without crash
- [ ] Orientation: rotate if `orientation` is `both` or `landscape`
- [ ] Theme: day/night if `themeModes` includes both
- [ ] Permission flows (if any) — deny then grant
- [ ] Offline / no-network graceful path if feature uses network
- [ ] Back stack / predictive back does not break primary flows
- [ ] No obvious ANR / frozen UI on list-heavy screens

Document: device/API, build variant, what was skipped.

## Output

```markdown
## Automated
- unit: Pass/Fail (details)
- integration: Pass/Fail / N/A (details)
- e2e (androidTest): Pass/Fail / Skipped (reason)

## Walkthrough
- ✅/❌ per checklist item

## Blockers
- …

## Suggested follow-ups
- test-unit / test-integration / test-e2e items to add
```

## Limits

- Do not invent production secrets or hit real paid APIs
- Do not claim full coverage if suites were skipped
- Emulator automation beyond Gradle connected tests is best-effort checklist unless the project already has UI automators approved
