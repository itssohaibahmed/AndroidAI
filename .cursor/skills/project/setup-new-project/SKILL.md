---
name: setup-new-project
description: Bootstrap a new Android multi-module app (domain, data, presentation, core-*) with MainActivity, nav_graph, and mandatory EntranceFragment as start destination. Use when starting a new project, converting a single-module app, or scaffolding the standard architecture.
---

# Setup New Project

Follow `.cursor/rules/` — especially `00-global`, `02-project-structure`, `07`, `08`, `17`, `19`, `23`.

## Preconditions (ask if missing)

1. `applicationId` / root package (e.g. `com.company.app`)
2. App display name
3. Optional extras: ads (`:gmaAds`), Firebase — **do not add without approval**

## Module set (mandatory)

| Module | Required | Role |
|--------|----------|------|
| `:app` | Must | `App`, manifest, DI aggregation (`KoinModules`) |
| `:domain` | Must | Entities, repository interfaces, use cases |
| `:data` | Must | Repository impls, DataSources |
| `:presentation` | Must | Screens, MVI, **nav graphs**, MainActivity host UI |
| `:core-common` | Required | `Constants` (TAGs), pure Kotlin shared types |
| `:core-ui` | Required | Themes, strings, Parent*, extensions, drawables |
| `:core-platform` | Required | InternetManager stub, dispatchers DI, platform helpers |

Optional later: `:feature-*`, `:gmaAds`.

```
app (Composition Root)
 |
 ↓
presentation → domain ← data
 |
 ↓
core-common / core-ui / core-platform
```

## Step 1 — Gradle

1. `settings.gradle.kts` — `include` all modules above
2. Root `build.gradle.kts` — plugins `apply false` via catalog
3. `libs.versions.toml` — add if missing: AndroidX Core/AppCompat/Material/ConstraintLayout, Fragment KTX, Lifecycle, Navigation (+ Safe Args), Koin, Coroutines, SplashScreen
4. Each library module: `alias(libs.plugins.android.library)`, `namespace = "<applicationId>.<layer>"`, minSdk 24, Java 17, View Binding on UI modules
5. Dependency graph:
   - `app` → presentation, data, domain, core-*
   - `presentation` → domain, core-* (**never** `:data`)
   - `data` → domain, core-* (prefer not `:core-ui`)
   - `domain` → coroutines-core only (+ optional pure feature libs)
   - `core-common` → no Android UI / Koin
6. Enable Navigation Safe Args on `:presentation`
7. Move launcher theme / strings into `:core-ui` (single `strings.xml`)

## Step 2 — Application + DI

```kotlin
// :app App.kt — DI only
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            lazyModules(KoinModules().getKoinModules())
        }
    }
}
```

- `KoinModules.getKoinModules()` aggregates: `appModule`, `coreModule`, `corePlatformModule`, `data` modules, `entrancePresentationModule`, …
- Manifest: `android:name=".App"`, `supportsRtl="true"`, splash theme on launcher if using SplashScreen API
- Prefer portrait **and** landscape — do not lock orientation

## Step 3 — MainActivity + host layout

- Prefer `MainActivity` in `:presentation` (FQN in `:app` manifest) or thin Activity in `:app` hosting presentation graph — match project convention once chosen
- Layout `activity_main.xml`:

```xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/fcvContainerMain"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph" />
```

- View Binding only; Material / ConstraintLayout; works in portrait + landscape

## Step 4 — Navigation (mandatory Entrance)

Create `presentation/.../res/navigation/nav_graph.xml`:

```xml
<navigation
    android:id="@+id/nav_graph"
    app:startDestination="@id/entranceFragment">

    <fragment
        android:id="@+id/entranceFragment"
        android:name="<applicationId>.presentation.entrance.ui.EntranceFragment"
        android:label="fragment_entrance"
        tools:layout="@layout/fragment_entrance" />
</navigation>
```

**Rules (every new project):**

- Start destination **must** be `entranceFragment`
- Class name **must** be `EntranceFragment`
- Feature folder: `presentation/entrance/`
- Layout: `fragment_entrance.xml`
- Do not use Home/Splash/Main as start destination — Entrance routes onward via Effects

## Step 5 — Entrance feature (minimal MVI)

Use `feature/create-mvi` patterns under `presentation/entrance/`:

```
entrance/
  di/EntrancePresentationModule.kt
  intent/EntranceIntent.kt
  state/EntranceState.kt
  effect/EntranceEffect.kt
  viewModel/EntranceViewModel.kt
  ui/EntranceFragment.kt
```

- Extend `ParentFragment` / `BaseFragment` when bases exist; otherwise create thin `ParentFragment` in `:core-ui` first
- Collect state/effects with lifecycle-aware collectors
- Typical Effects later: navigate to language / onboarding / dashboard — leave actions empty until those screens exist
- Register `entrancePresentationModule` in `KoinModules`
- Strings in `:core-ui` only

## Step 6 — Core stubs (required)

### `:core-common`
```kotlin
object Constants {
    const val TAG = "TAG_App"
    const val TAG_ADS = "TAG_ADS"
    const val TAG_FIREBASE = "TAG_FIREBASE"
    const val TAG_REMOTE_CONFIG = "TAG_REMOTE_CONFIG"
}
```

### `:core-ui`
- Material3 DayNight theme
- Empty/minimal `ParentFragment` + `ParentActivity` with ViewBinding lifecycle
- `strings.xml` (app_name + entrance placeholders)
- Lifecycle Flow collection extensions (`collectWhenStarted` / `collectWhenCreated`)

### `:core-platform`
- DI module providing `Dispatchers.IO` / `Dispatchers.Default` (named if project uses named dispatchers)
- Optional `InternetManager` stub

### `:data` / `:domain`
- Empty packages + `DataModule` / placeholder ready for `SharedPrefManager` later
- No fake business logic

## Step 7 — Verify

- [ ] `settings.gradle.kts` includes app, domain, data, presentation, core-common, core-ui, core-platform
- [ ] `presentation` does not depend on `:data`
- [ ] `nav_graph` `startDestination` = `@id/entranceFragment`
- [ ] `EntranceFragment` + `fragment_entrance.xml` exist
- [ ] MainActivity hosts `fcvContainerMain` + `nav_graph`
- [ ] App starts Koin; `entrancePresentationModule` registered
- [ ] Strings/themes in `:core-ui`
- [ ] Project builds (`assembleDebug`)
- [ ] Portrait + landscape OK on entrance

## Do not

- Start with Compose / Data Binding / Hilt
- Put start destination on any screen other than Entrance
- Add Room / Retrofit / Ads / Firebase unless approved
- Hardcode secrets or lock `screenOrientation` unless product requires it

## After setup

Next features: language / onboarding / home via `feature/create-mvi` + wire actions from Entrance Effects in `nav_graph.xml`.
