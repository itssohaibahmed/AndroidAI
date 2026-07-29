---
name: setup-new-project
description: Bootstrap a new Android multi-module app (domain, data, presentation, core-*) with MainActivity, nav_graph, EntranceFragment, Parent* bases, PlatformFirebase object, and Remote Config → SharedPreferences cache. Use when starting a new project or converting a single-module app.
---

# Setup New Project

Follow `.cursor/rules/` — especially `00-global`, `02`, `07`, `08`, `09`, `17`, `19`, `22`, `23`.

## Preconditions (ask if missing)

1. `applicationId` / root package (e.g. `com.company.app`)
2. App display name
3. Optional: Firebase / ads — **do not add SDKs without approval**; still scaffold stubs/interfaces as below when Firebase is approved

## Module set (mandatory)

| Module | Required | Role |
|--------|----------|------|
| `:app` | Must | `App`, manifest, DI aggregation only — **no `res/values/`** |
| `:domain` | Must | Entities, repository interfaces, use cases |
| `:data` | Must | Repository impls, DataSources, SharedPref + RC cache |
| `:presentation` | Must | Screens, MVI, nav graphs, MainActivity host UI |
| `:core-common` | Required | `Constants` (TAGs), `EventsProvider` |
| `:core-ui` | Required | **All** themes/strings/colors/splash, Parent*, extensions |
| `:core-platform` | Required | `InternetManager`, `PlatformFirebase`, dispatchers DI |

```
app (Composition Root) — no values resources
 |
 ↓
presentation → domain ← data
 |
 ↓
core-common / core-ui / core-platform
```

## Step 1 — Gradle

1. `settings.gradle.kts` — `include` all modules above
2. Root plugins `apply false` via catalog; **latest stable** versions
3. Catalog sections/naming per `08-gradle.mdc`
4. Dependency graph: `presentation` **never** → `:data`; `domain` → coroutines only
5. View Binding on UI modules; Safe Args on `:presentation`
6. **Remove** `:app` `src/main/res/values/` (and night) — move themes/strings/colors/themes into `:core-ui`
7. `:app` may keep only `mipmap` / `xml` backup rules if needed — **no** `strings.xml` / `themes.xml` / `colors.xml` at app level
8. **Every module** gets a `.gitignore`: libraries → `/build`; `:app` → `/build` + `/release` (see `02-project-structure`)

## Step 2 — Application + DI

```kotlin
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

- Aggregate with **`lazyModule` only**: `appModule`, `coreModule`, `corePlatformModule`, `dataModule`, `useCaseModule`, `entrancePresentationModule`, …
- Manifest: `android:name=".App"`, `android:theme="@style/Theme.App.Starting"`, `supportsRtl="true"`
- Portrait **and** landscape — do not lock orientation
- UseCases + repo interfaces → `:domain`; DataSources + repo impls → `:data` (`dataModule` with `//// DataSources` / `//// Repositories`)

## Step 3 — MainActivity + host

- `MainActivity` extends `ParentActivity` in `:presentation`
- `activity_main.xml` with `fcvContainerMain` + `NavHostFragment` + `@navigation/nav_graph`
- Call `installSplashTheme()` in `onPreCreated()` when using splash

## Step 4 — Navigation (mandatory Entrance)

`nav_graph.xml` **must** use:

```xml
app:startDestination="@id/entranceFragment"
```

- Class: `EntranceFragment` under `presentation/entrance/ui/`
- Layout: `fragment_entrance.xml`
- No Home/Splash/Main as start destination

### Nav transition anims (reference — mandatory)

Create in **`:core-ui`** (copy from [templates/anim/](templates/anim/) + [templates/anim-ldrtl/](templates/anim-ldrtl/)):

```
res/anim/slide_in_right.xml | slide_out_left.xml | slide_in_left.xml | slide_out_right.xml
res/anim-ldrtl/… (same four names — RTL mirrors)
```

Every forward `<action>` must include:

```xml
app:enterAnim="@anim/slide_in_right"
app:exitAnim="@anim/slide_out_left"
app:popEnterAnim="@anim/slide_in_left"
app:popExitAnim="@anim/slide_out_right"
```

See `17-navigation.mdc`.

## Step 5 — Entrance MVI

Scaffold `presentation/entrance/{di,intent,state,effect,viewModel,ui}` via create-mvi patterns.

- `EntranceFragment` extends `ParentFragment` (then `BaseFragment` when that layer exists)
- Register `entrancePresentationModule` in `KoinModules`
- Strings only in `:core-ui`

## Step 6 — `:core-ui` Parent* bases (required)

**Canonical source:** [templates/base/](templates/base/) (Qibla-aligned; `ParentSheet` null-safe).

Copy templates → replace `YOUR.PACKAGE` with applicationId root:

```
core-ui …/base/
  activity/ParentActivity.kt
  fragment/ParentFragment.kt
  dialog/ParentDialogDismissal.kt + ParentDialog.kt
  sheet/ParentSheetDismissal.kt + ParentSheet.kt
core-ui …/extensions/
  FlowCollectionExtensions.kt   # collectWhenStarted / collectWhenCreated

presentation …/base/
  activity/BaseActivity.kt
  fragment/BasePermissionFragment.kt + BaseFragment.kt
  sheets/BaseDialog.kt + BaseSheet.kt
```

See [templates/base/README.md](templates/base/README.md) for hierarchy and notes.

### ParentFragment
- Generic `ViewBinding` + `bindingFactory`
- Clear `_binding` in `onDestroyView`
- Hooks: `initObservers()`, `onViewCreated()`, abstract `onViewCreated()`

### ParentActivity
- Generic `ViewBinding` + edge-to-edge + window insets padding flags
- `installSplashTheme()` → `installSplashScreen()`
- Abstract `onCreated()`; optional `onPreCreated()` / `initObservers()`

### ParentDialog (+ Dismissal)
- `ParentDialogDismissal` : `DialogFragment` with `onDismissCallback` + `safeShow` / `safeDismiss` helpers
- `ParentDialog` : ViewBinding via `MaterialAlertDialogBuilder.setView(binding.root)`
- Null-safe binding; clear in `onDestroyView`

### ParentSheet (+ Dismissal)
- `ParentSheetDismissal` : `BottomSheetDialogFragment` with `onDismissCallback` + `safeShow` / `safeDismiss` helpers
- `ParentSheet` : inflate with `bindingFactory`, null-safe `_binding` (same as Fragment — **not** `!!`), `onSheetCreated()`, `initObservers()`

### presentation Base*
- `BasePermissionFragment` → `BaseFragment` → feature screens
- `BaseActivity` / `BaseDialog` / `BaseSheet` — thin wrappers; add ads/billing injects on `BaseFragment`/`BaseActivity` only when product needs them

Also add: `themes.xml` (include `ButtonStyle.Icon` / `ButtonStyle.Icon.Only`), **`splash.xml`**, `strings.xml` / `colors.xml` with **app → general → screen-wise** sections (`09-resources-xml`).

## Step 7 — `:core-platform`

### Dispatchers (no named qualifiers)

```kotlin
val corePlatformModule = lazyModule {

    //// Dispatchers
    single { Dispatchers.IO }
    single { Dispatchers.Default }

    //// Managers
    single { InternetManager(androidContext()) }
}
```

Repository injects by type:

```kotlin
class XRepositoryImpl(
    private val dataSource: XDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
)
```

Koin resolves `Dispatchers.IO` / `Dispatchers.Default` as distinct `CoroutineDispatcher` instances — **do not** use `named("io")` / `named("default")`.

> If both are type `CoroutineDispatcher`, prefer constructor defaults `= Dispatchers.IO` in repos (reference style) **or** inject only IO via DI and use `Dispatchers.Default` explicitly for CPU work. Do **not** introduce named qualifiers.

### PlatformFirebase — `object`, no Context

```kotlin
object PlatformFirebase {

    fun Throwable.recordException(log: String) { /* Crashlytics + Log */ }

    fun String.postFirebaseEvent() { /* Analytics bundle + TAG_FIREBASE log */ }

    fun getDeviceToken() { /* FirebaseInstallations token log */ }
}
```

- **No** `Context` constructor / property on the object
- Live in `:core-platform` `firebase/PlatformFirebase.kt`
- Event name constants in `:core-common` `EventsProvider`
- Ads revenue logging (if needed later): keep out of this object or pass primitives only — do not bake Context into `PlatformFirebase`

### InternetManager
- Connectivity check used by RC / network repos

## Step 8 — SharedPreferences + Remote Config cache

### SharedPref (`data/sharedPreferences/`)

- `SharedPrefManager(context)` — **sync only, no dispatcher** (see `data/shared-preferences` skill)
- Domain `SharedPrefRepository` + `SharedPrefRepositoryImpl` with `withContext(ioDispatcher)`
- Include RC cache properties (ints/bools/strings) written by RC repository

### Remote Config (cache-to-prefs architecture)

```
domain: RemoteConfigRepository { suspend fun fetchAndCache(): Boolean }
data:
  remoteConfig/dataSource/RemoteConfigDataSource.kt   # Firebase RC only
  remoteConfig/repository/RemoteConfigRepositoryImpl.kt
```

**RemoteConfigDataSource** (no dispatcher):
- `minimumFetchIntervalInSeconds(0)` always
- `fetchAndActivate()`, live update listener, typed `getInt` / `getBoolean` / `getString`
- Mutex around fetch; log with `TAG_REMOTE_CONFIG`

**RemoteConfigRepositoryImpl** (dispatcher here):
1. Check `InternetManager`
2. `remoteDataSource.fetchAndActivate()`
3. **`saveValues()`** — copy every needed RC key into `SharedPrefManager` properties (cache)
4. Register live listener → `saveValues()` again on update
5. If fetch fails, prefs still hold last cache — app reads cache via `SharedPrefRepository`

**Read path for features:** Prefer **cached prefs** (`SharedPrefRepository` / managers) for flags used at runtime — not live RC SDK in UI.

**DI (`lazyModule` — sectioned by concern):**
```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { RemoteConfigDataSource() }
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = lazyModule {

    //// RemoteConfig
    factory { FetchRemoteConfigUseCase(get()) }
}
```

- Interfaces + `FetchRemoteConfigUseCase` in **`:domain`**
- Register both modules in `KoinModules`

Wire `FetchRemoteConfigUseCase` and call early from Entrance / App startup flow (non-blocking UX).

## Step 9 — Verify

- [ ] Every module has `.gitignore` (`/build`; `:app` also `/release`)
- [ ] No `:app/src/main/res/values/` (themes/strings/colors live in `:core-ui`)
- [ ] Modules: app, domain, data, presentation, core-common, core-ui, core-platform
- [ ] UseCases + repo interfaces only in `:domain`; `dataModule` has `//// DataSources` then `//// Repositories`
- [ ] All DI uses `lazyModule` (never `module { }`); `useCaseModule` in domain registered in `KoinModules`
- [ ] `nav_graph` startDestination = `entranceFragment`
- [ ] `:core-ui` has `anim/` + `anim-ldrtl/` slide_* set; nav actions use the four anim attrs
- [ ] ParentActivity / ParentFragment / ParentDialog / ParentSheet (+ Dismissal) copied from `templates/base/`
- [ ] BaseActivity / BaseFragment / BasePermissionFragment / BaseDialog / BaseSheet + Flow collection extensions
- [ ] `PlatformFirebase` is `object` without Context
- [ ] Dispatchers registered **without** `named("io")` / `named("default")`
- [ ] RC `minimumFetchIntervalInSeconds(0)` + cache write to `SharedPrefManager`
- [ ] `presentation` ↛ `:data`
- [ ] `assembleDebug` succeeds; portrait + landscape OK

## Do not

- Compose / Data Binding / Hilt
- App-level `values` resources
- Named dispatcher qualifiers
- `PlatformFirebase` holding Context
- Reading RC only from SDK in Fragments (use prefs cache)
- Hardcode secrets / lock orientation unless product requires

## After setup

Next: language / onboarding / home via `create-mvi`; wire Entrance Effects in `nav_graph.xml`.
