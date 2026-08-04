---
name: gradle-organize
description: Organize Gradle module dependencies and libs.versions.toml into standard comment sections and catalog naming. Use when cleaning build.gradle.kts, fixing dependency order, restructuring the version catalog, or the user asks to organize Gradle.
---

# Gradle Organize

Follow `.cursor/rules/08-gradle.mdc`. Match the standard section headers and order below.

## When to run

- Messy / unsorted `dependencies { }` blocks
- Catalog missing section comments or inconsistent aliases
- New libs added at the bottom out of section
- User says “organize gradle” / “fix dependency sections”

## Scope

1. `gradle/libs.versions.toml`
2. Every module `build.gradle.kts` (`app`, `presentation`, `data`, `domain`, `core-*`, ads, features)
3. Do **not** change versions unless user also asks to bump — organizing ≠ upgrading (use `gradle-update` for bumps)

---

## Step 1 — Organize `libs.versions.toml`

### Tables (always these three, in order)

1. `[versions]`
2. `[plugins]`
3. `[libraries]`

### `[versions]` layout

```toml
[versions]
# -------------- Plugins -------------- #
agp = "…"
kotlin = "…"
# google services / crashlytics gradle / safe-args plugin versions…

# -------------- Dependencies -------------- #
coreKtx = "…"
appcompat = "…"
material = "…"
activity = "…"
constraintlayout = "…"

# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Coroutine (for await) calls
# Kotlin Coroutines (pure Kotlin; used by domain for Flow)
# Dependency Injection -> Koin
# CameraView
# Glide
# Lottie Animations
# Dots Indicator
# Shimmer Effect
# Language / time APIs
# Testing
```

### `[libraries]` — same section comments, matching order

Put Android Core libs **first without a header** (or under an implied core group), then:

```toml
# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Maps
# Kotlin Coroutines
# Dependency Injection -> Koin
# CameraView
# Glide
# Lottie Animations
# Dots Slider
# Shimmer
# java.time desugaring for API < 26
# Testing
```

### Naming (fix while organizing)

| Kind | Convention | Example |
|------|------------|---------|
| Version key | camelCase | `coreKtx`, `koinAndroid` |
| Library alias | kebab-case | `androidx-core-ktx` → `libs.androidx.core.ktx` |
| Plugin alias | kebab-case | `android-application`, `navigation-safe-args` |

- One shared version key per family (`lifecycle` for viewmodel/runtime/process)
- Prefer `group` + `name` + `version.ref`
- Comment out unused with `#` — keep under the correct section
- **Never** leave hardcoded `"g:a:v"` in module scripts — move into catalog

---

## Step 2 — Organize each module `dependencies { }`

### Block order (mandatory)

1. **Project modules first** (no header if few; optional blank line after)
2. Library sections with `//` headers
3. Omit empty sections

### Canonical section order (use exact header text when present)

```kotlin
dependencies {
    implementation(project(":…"))
    // feature-* / core-* / domain / data / gmaAds as allowed by module boundaries

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Splash Screen Api

    // Lifecycle

    // Fragment Ktx

    // Navigational Components

    // Google

    // Google Play Services, Maps & Location
    // (or shorter: // Google Play Services Location)

    // Firebase

    // Dependency Injection -> Koin
    // (short form OK in small modules: // Koin)

    // Kotlin Coroutines
    // (domain often: // Core coroutine support for Flows)

    // CameraView

    // Lottie Animation

    // Glide

    // Dots Slider

    // Shimmer

    // Testing

    // Core library desugaring (java.time APIs support on older APIs)
}
```

### Per-module expectations (reference)

| Module | Typical sections |
|--------|------------------|
| `:app` | projects → Android Core → Firebase → Koin → Testing → Desugaring |
| `:presentation` | projects → Android Core → Lifecycle → Fragment → Navigation → Google → Play Services → Firebase → Koin → Camera/Lottie/Glide/Dots/Shimmer |
| `:data` | projects → Android Core → Google → Firebase → Location → Koin |
| `:core-ui` | projects → Android Core → Splash → Lifecycle → Navigation → Google → Firebase → Koin → Glide |
| `:core-platform` | projects → Android Core → Firebase → Play Services Location → Koin |
| `:gmaAds` | projects → Android Core → Lifecycle → Google (`api` ads) → Koin |
| `:domain` | Coroutines + Koin DSL for `useCaseModule` (+ optional pure `project(":feature-*")`) |

### Scopes while organizing

- Keep `implementation` as default
- Preserve existing `api` (e.g. ads mediation) — do not silently change to `implementation`
- Keep `testImplementation` / `androidTestImplementation` under `// Testing`
- Keep `coreLibraryDesugaring` under desugaring comment

### Module script shape

```
plugins { }
android { }
dependencies { }   // organized sections only
```

Do not reorder unrelated `android { }` logic unless asked.

---

## Step 3 — Verify

- [ ] Catalog has Plugins + Dependencies section banners under `[versions]`
- [ ] `[libraries]` section comments align with module dependency headers
- [ ] Every module: project deps first, then sectioned libs
- [ ] No hardcoded Maven coordinates in `*.gradle.kts`
- [ ] Aliases kebab-case; version keys camelCase
- [ ] Sync/build still works (`assembleDebug` if practical)
- [ ] Module boundaries unchanged (`presentation` still must not depend on `:data`)

## Report to user

```markdown
## Gradle organize summary
- Catalog: …
- Modules touched: …
- Moved / regrouped: …
- Left unchanged (versions / api scopes): …
```

## Do not

- Add new libraries without approval (`13-libraries-stack`)
- Bump to latest unless user asked
- Invent new section header names when an existing header already matches
- Break `api` vs `implementation` semantics
