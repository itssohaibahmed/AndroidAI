---
name: test-ui
description: Write or extend Android instrumentation / Espresso UI tests for critical user-visible flows. Use when asking for UI tests, androidTest, or Espresso. No new libraries without approval. Part of test-* suite.
---

# UI / Instrumentation Tests

Follow `.cursor/rules/11-testing.mdc`.

Obey `.cursor/project-settings.json` (`orientation`, `writeTestsWithFeatures`).

## Scope

- Critical user-visible flows only (entrance → primary feature, paywall, permission deny/grant UX)
- Prefer existing project `androidTest` stack — **do not** add Espresso/Compose/UI libs without approval

## Rules

- Use project's `testInstrumentationRunner`
- Keep tests independent — no shared mutable state between tests
- Assert user-visible behavior, not private ViewModel fields
- Match orientation policy from `project-settings.json` when relevant
- Strings via resources — avoid brittle hardcoded copy when `@string/` ids are stable

## Location

`src/androidTest/java` mirroring packages where practical.

## Forbidden

- Instrumentation for pure business logic (use `test-unit` / `test-flow`)
- New UI-test dependencies without human approval
- Flaky sleeps — prefer IdlingResource / Espresso sync already in project
