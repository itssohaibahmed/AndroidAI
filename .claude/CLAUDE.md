# AndroidAI — Claude Code

Production Android app. Kotlin, XML + View Binding, Clean Architecture, MVVM + MVI (Intent / State / Effect), Koin `lazyModule` only. No Compose, no Data Binding, no Hilt unless the project already uses it.

## Law

Follow [`.claude/rules/`](rules/) (especially `00-global`, `14-security-secrets`, `16-logging`). Path-scoped rules load when matching files are touched. Full patterns: [`.claude/rules/reference/`](rules/reference/).

When [`.claude/project-settings.json`](project-settings.json) exists, obey `writeTestsWithFeatures`, `orientation`, `themeModes`, `applicationId`, `appName`.

## Typical feature flow

1. `/setup-new-project` (greenfield) or `/setup-old-project` (existing production app)
2. `/figma-to-xml` (or `/create-dialog` / `/create-bottom-sheet`) — XML only
3. `/create-mvi` — presentation Intent / State / Effect / ViewModel / Fragment
4. `/create-clean-architecture` — only when new domain / data is required
5. `/review-complete` before PR

## Skills

Invoke with `/` using the skill folder name (`/create-mvi`, `/figma-to-xml`, …). Full map and rules index: [README.md](README.md).