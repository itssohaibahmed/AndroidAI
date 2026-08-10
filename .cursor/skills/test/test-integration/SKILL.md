---
name: test-integration
description: Write multi-layer integration tests (Repository + Room in-memory, Retrofit + MockWebServer, etc.). Use when testing real data-layer wiring across components. Not for fakes-only unit tests (test-unit) or Espresso UI (test-e2e). Part of test-* suite.
---

# Integration Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write/update multi-layer integration tests (e.g. Room in-memory / MockWebServer) — no device if JVM-only; if `androidTest` is required we will need an emulator or physical device attached.

If the work must use `androidTest` and no device is attached: say so after the banner, continue with any JVM-safe parts, and mark device steps blocked.

Follow `.cursor/rules/11-testing.mdc`, `26-data-persistence.mdc`, and matching `rules/reference/` (Room / Retrofit) when relevant.

Obey `.cursor/project-settings.json` (`writeTestsWithFeatures`).

## Scope

- Repository (or similar) wired to **real** lower layers:
  - Room **in-memory** database + DAOs
  - Retrofit + **MockWebServer** (or project-approved equivalent already in catalog)
- DAO / migration smoke when the change touches schema
- Assert cross-layer behavior (mapping, persistence, error mapping), not UI

Prefer `src/test` JVM. Use `src/androidTest` **only** when the layer requires Android runtime — then restate that an emulator or physical device is required (or proceed with one already attached).

## Naming

```
<Unit>_when_<condition>_then_<result>
```

## Approach

1. Build the smallest real stack needed (e.g. in-memory Room + repository)
2. Drive through the repository (or UseCase that only exists to orchestrate data) — not Fragments
3. Tear down DB / server after each test; keep tests independent
4. No production secrets, no real paid APIs
5. Add libraries only if already in the version catalog **and** approved — do not invent new deps

## Forbidden

- Fakes-only single-class tests → `test-unit`
- Espresso / user-visible UI flows → `test-e2e`
- Hitting real production backends or committing secrets
- Flaky sleeps instead of deterministic server/DB setup
