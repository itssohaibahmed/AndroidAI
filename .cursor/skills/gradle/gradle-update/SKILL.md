---
name: gradle-update
description: Update all Gradle / version-catalog dependencies to the latest compatible stable versions. Use when the user asks to bump dependencies, update libs.versions.toml, or gradle-update. Does not add new libraries without approval.
---

# Gradle Update

Follow `.cursor/rules/08-gradle.mdc`, `13-libraries-stack.mdc`.

Organizing section order is **`gradle-organize`** — this skill **bumps versions**.

## Scope

1. `gradle/libs.versions.toml` — `[versions]` (and plugin/library refs that share them)
2. Root / module scripts only if a plugin id or apply style must change for the new version
3. **Do not** add new libraries without explicit human approval
4. Prefer **latest stable** — no alphas/betas/RCs unless user asks

## Steps

1. Inventory current catalog versions
2. Look up latest stable for each (AGP ↔ Kotlin ↔ library compatibility matrix)
3. Bump version keys carefully:
   - Keep one shared key per family (`lifecycle`, `koin`, …)
   - Update AGP/Kotlin/KSP together when required
4. Sync / assemble if practical (`assembleDebug`)
5. Fix breakages (deprecated APIs, KSP args, plugin DSL) with minimal diffs
6. If sections are messy after bumps, run **`gradle-organize`** logic (or tell user to invoke it)

## Report

```markdown
## Gradle update summary
- Bumped: …
- Left unchanged (already latest / blocked): …
- Compatibility notes: …
- Build: Pass/Fail/Not run
```

## Do not

- Add Compose / Hilt / new stacks (`13-libraries-stack`)
- Change `api` vs `implementation` casually
- Force incompatible AGP/Kotlin pairs
- Commit secrets from `local.properties`
