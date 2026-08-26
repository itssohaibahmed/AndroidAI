---
description: Gradle, build config, catalog sections/naming, and dependency management
paths:
  - "**/*.gradle.kts"
  - "**/libs.versions.toml"
  - "**/gradle.properties"
---

# Gradle (invariants)

**Full detail (android section order, signingConfigs, bundle, `base`, catalog, ProGuard):** [reference/gradle.md](reference/gradle.md)

## Must follow

- Root `build.gradle.kts`: plugins `apply false` only
- Prefer **Kotlin DSL only** (`*.gradle.kts`). If Groovy `*.gradle` / `settings.gradle` remain, convert before organize/update (`gradle-update` Step 0 / `setup-old-project` Step 2)
- Module order: `plugins` → `android` → (`base` on `:app` only) → `dependencies`
- `:app` `android` order: `namespace` → `compileSdk` → `defaultConfig` → **`signingConfigs`** → `buildTypes` → `buildFeatures` → `compileOptions` → `kotlin`/`jvm` (only if already used) → **`bundle`**
- Library modules: same order; **omit** `signingConfigs`, `bundle`, `base`, and app-only `defaultConfig` fields
- Always add `:app` `signingConfigs` if missing — find `*.jks` in root then `app/`; empty strings if none
- Always add `:app` `bundle { language { enableSplit = false } }`
- `:app` `base { archivesName = "AppName-Account-v{versionCode}({versionName})" }`
- Dependencies: project modules first, then section headers (Android Core, Lifecycle, Firebase, Koin, Testing, …)
- Version catalog: `[versions]` / `[plugins]` / `[libraries]`; camelCase version keys; kebab-case aliases; **never** hardcode `g:a:v` in modules
- **`gradle-update` must** convert Groovy → Kotlin DSL when needed; bump catalog **and** any hardcoded deps; migrate hardcodes into `libs.versions.toml` (create file if missing); place under `gradle-organize` section headers (e.g. Glide → `// Glide`, not under Testing)
- Latest stable on add/update; Java 17; minSdk 24; compileSdk/targetSdk **37**; xml UI modules: View Binding on; compose modules: `buildFeatures { compose = true }` + `kotlin.compose` plugin — never Data Binding; app release minify on
- Signing secrets: empty placeholders or `local.properties` / CI — never commit real keystore passwords into docs/skills
- ProGuard keeps: domain entities + presentation state/intent/effect/model (+ ads when present)
- Changing Gradle law here → also update `gradle-update`, `gradle-organize`, `setup-old-project` Groovy→KTS steps, `reference/gradle.md`, and `.cursor` twins

Read [reference/gradle.md](reference/gradle.md) when editing Gradle or the catalog.
