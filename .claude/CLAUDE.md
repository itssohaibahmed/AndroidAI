# AndroidAI — Claude Code

Production Android app. Kotlin, Clean Architecture, MVVM + MVI (Intent / State / Effect), Koin `lazyModule` only. UI from `.claude/project-settings.json` **`uiFramework`**: `xml` (View Binding + `:presentation`) or `compose` (Jetpack Compose + `:feature-*`). Never Data Binding. No Hilt unless the project already uses it.

## Law

Follow [`.claude/rules/`](rules/) (especially `00-global`, `14-security-secrets`, `16-logging`). Path-scoped rules load when matching files are touched. Full patterns: [`.claude/rules/reference/`](rules/reference/). Compose packaging: `28-compose-ui` + [reference/compose-ui.md](rules/reference/compose-ui.md).

When [`.claude/project-settings.json`](project-settings.json) exists, obey `uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`, `applicationId`, `appName`, optional `figmaDesignSystemUrl`.

## Typical feature flow

**Shortcut:** `/create-screen` runs UI + optional domain/data + MVI in one invoke.

1. `/setup-new-project` (greenfield) or `/setup-old-project` (existing production app) — persist **`uiFramework`**
2. `/setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `/figma-to-xml` if xml; `/figma-to-compose` if compose (or `/create-dialog` / `/create-bottom-sheet`)
4. `/create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen` in `:feature-*`
5. `/create-clean-architecture` — only when new domain / data is required
6. `/review-complete` before PR

## Skills

Invoke with `/` using the skill folder name (`/create-screen`, `/create-mvi`, `/figma-to-xml`, `/figma-to-compose`, …). Full map and rules index: [README.md](README.md).