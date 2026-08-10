---
name: test-e2e
description: Write or extend Android E2E instrumentation / Espresso tests for critical user-visible flows. Use when asking for E2E, UI instrumentation, androidTest, or Espresso. Requires emulator or physical device. No new libraries without approval. Part of test-* suite.
---

# E2E / Instrumentation Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write/update E2E instrumentation tests — an emulator or physical device is required (or we proceed with the device already attached).

If no device is attached: say so after the banner, do not claim E2E ran on device, and leave a clear blocker.

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

- Instrumentation for pure business logic → `test-unit`
- Multi-layer data wiring without UI → `test-integration`
- New UI-test dependencies without human approval
- Flaky sleeps — prefer IdlingResource / Espresso sync already in project
