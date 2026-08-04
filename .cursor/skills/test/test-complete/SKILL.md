---
name: test-complete
description: Run all written unit/flow/UI tests and perform an emulator walkthrough checklist of the app. Use when asking to test everything, full test pass, or emulator go-through.
---

# Complete Test Pass

Follow `.cursor/rules/11-testing.mdc`.

## Step 1 — Run existing automated suites

From project root (Gradle):

```bash
./gradlew test
./gradlew connectedDebugAndroidTest
```

On Windows PowerShell, use `.\gradlew.bat` equivalents.

- Report pass/fail per module
- If `connectedDebugAndroidTest` cannot run (no device), note blocker and continue with unit results + walkthrough

## Step 2 — Align with test skills

- Gaps in UseCase/ViewModel coverage → suggest `test-unit`
- Flow/repository gaps → `test-flow`
- Critical UX gaps → `test-ui`
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
- androidTest: Pass/Fail / Skipped (reason)

## Walkthrough
- ✅/❌ per checklist item

## Blockers
- …

## Suggested follow-ups
- test-unit / test-flow / test-ui items to add
```

## Limits

- Do not invent production secrets or hit real paid APIs
- Do not claim full coverage if suites were skipped
- Emulator automation beyond Gradle connected tests is best-effort checklist unless the project already has UI automators approved
