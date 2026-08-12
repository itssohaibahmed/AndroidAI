---
name: setup-new-project
description: Bootstrap a new Android multi-module app (domain, data, presentation, core-*) with MainActivity, nav_graph, EntranceFragment, Parent* bases, PlatformFirebase object, Remote Config → SharedPreferences cache, and mandatory firebase-messaging on core-platform. Use when starting a new project or converting a single-module app. Confirms and persists project settings first.
---

# Setup New Project

Follow `.cursor/rules/` — especially `00-global`, `02-project-structure`, `07-dependency-injection`, `08-gradle`, `09-resources-xml`, `17-navigation`, `19-base-ui`, `22-platform-firebase`, `23-app-startup`, `26-data-persistence`.

## Preconditions (ask if missing — then persist)

Confirm with the user before scaffolding. Write answers to **`.cursor/project-settings.json`**:

```json
{
  "writeTestsWithFeatures": true,
  "orientation": "both",
  "themeModes": "both",
  "applicationId": "com.company.app",
  "appName": "App Display Name"
}
```

| Setting                        | Allowed values                    | Effect                                                         |
|--------------------------------|-----------------------------------|----------------------------------------------------------------|
| `applicationId` / root package | e.g. `com.company.app`            | Module namespaces, package roots                               |
| `appName`                      | display name                      | Launcher label / strings                                       |
| `writeTestsWithFeatures`       | `true` / `false`                  | Whether later skills add UseCase/ViewModel tests with features |
| `orientation`                  | `portrait` / `landscape` / `both` | Layout orientation support (default `both`)                    |
| `themeModes`                   | `day` / `night` / `both`          | Theme resource folders (default `both`)                        |

Also ask:

1. Optional: **ads** — do not add ad SDKs without approval
2. **Firebase Cloud Messaging** is **mandatory** for every new project: add `firebase-messaging` to the catalog + `implementation` on `:core-platform` only (dependency — no `FirebaseMessagingService` or push UI). See **`implement-firebase-messaging`**.

All later skills **must read** `.cursor/project-settings.json` and obey it.

## Module set (mandatory)

| Module           | Required | Role                                                        |
|------------------|----------|-------------------------------------------------------------|
| `:app`           | Must     | `App`, manifest, DI aggregation only — **no `res/values/`** |
| `:domain`        | Must     | Entities, repository interfaces, use cases                  |
| `:data`          | Must     | Repository impls, DataSources, SharedPref + RC cache        |
| `:presentation`  | Must     | Screens, MVI, nav graphs, MainActivity host UI              |
| `:core-common`   | Required | `Constants` (TAGs), `EventsProvider`                        |
| `:core-ui`       | Required | **All** themes/strings/colors/splash, Parent*, extensions   |
| `:core-platform` | Required | `InternetManager`, `PlatformFirebase`, dispatchers DI, **`firebase-messaging` dep** |

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

Follow `08-gradle.mdc` + [reference/gradle.md](../../../rules/reference/gradle.md) (canonical `:app` / library scripts) and **`gradle-organize`** for catalog + dependency sections.

1. `settings.gradle.kts` — `include` all modules above
2. Root plugins `apply false` via catalog; **latest stable** versions
3. Catalog sections/naming per `08-gradle.mdc` / `gradle-organize`
4. Dependency graph: `presentation` **never** → `:data`; `domain` → coroutines only
5. View Binding on UI modules; Safe Args on `:presentation`
6. **Remove** `:app` `src/main/res/values/` (and night) — move themes/strings/colors/themes into `:core-ui`
7. `:app` may keep only `mipmap` / `xml` backup rules if needed — **no** `strings.xml` / `themes.xml` / `colors.xml` at app level
8. **Every module** gets a `.gitignore`: libraries → `/build`; `:app` → `/build` + `/release` (see `02-project-structure`)
9. **Firebase Messaging (mandatory):** catalog entry + `implementation(libs.firebase.messaging)` on **`:core-platform`** under `// Firebase` — latest stable; **no** service/manifest/token code (see **`implement-firebase-messaging`**)

Organize dependency sections with **`gradle-organize`**.

### Module scripts (scaffold from reference)

Copy the `:app` and library shapes from [reference/gradle.md](../../../rules/reference/gradle.md) — module order `plugins` → `android` → (`base` on `:app`) → `dependencies`.

**`:app` must include**

| Item | Rule |
|------|------|
| `android` section order | `defaultConfig` → `signingConfigs` → `buildTypes` → `buildFeatures` → `compileOptions` → (`jvm` only if used) → `bundle` |
| `signingConfigs` | Always — search `*.jks` in root then `app/`; set `storeFile` if found; else empty strings; do not invent passwords |
| `bundle` | Always `language { enableSplit = false }` |
| `base.archivesName` | `AppName-Account-v{versionCode}({versionName})` from `project-settings.json` `appName` + account when known |

**Library modules** (`:presentation`, `:data`, `:domain`, `:core-*`): same relative order; **omit** `signingConfigs`, `bundle`, `base`, and app-only `defaultConfig` fields. UI modules get View Binding; `:domain` / `:core-common` may omit `buildFeatures`.

## Step 2 — Application + DI

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            lazyModules(KoinModules().getKoinModules())
        }
        // Theme / DynamicColors after Koin — never use GlobalContext.getOrNull()
        applyAppTheme()
    }
}
```

- Aggregate with **`lazyModule` only** (convert any `module` → `lazyModule`, `modules` → `lazyModules`): `appModule`, `coreModule`, `corePlatformModule`, `dataModule`, `useCaseModule`, `entrancePresentationModule`, …
- Theme: apply **after** `startKoin` in Application; Activity DynamicColors needs no GlobalContext gate (`07-dependency-injection`, `23-app-startup`)
- Manifest: `android:name=".App"`, `android:theme="@style/Theme.App.Starting"`, `supportsRtl="true"`
- Orientation: follow `project-settings.json` — default portrait **and** landscape; do not lock unless `orientation` is single-mode and product requires lock
- Theme modes: create `values` / `values-night` per `themeModes`
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
app:enterAnim="@anim/slide_in_right"app:exitAnim="@anim/slide_out_left"app:popEnterAnim="@anim/slide_in_left"app:popExitAnim="@anim/slide_out_right"
```

See `17-navigation.mdc`.

## Step 5 — Entrance MVI

Scaffold `presentation/entrance/{di,intent,state,effect,viewModel,ui}` via **`create-mvi`** patterns (presentation only). Domain/data for RC/prefs already from Steps 7–8.

- `EntranceFragment` extends `ParentFragment` (then `BaseFragment` when that layer exists)
- Register `entrancePresentationModule` in `KoinModules`
- Strings only in `:core-ui`
- Tests: only if `writeTestsWithFeatures` is `true`

## Step 6 — `:core-ui` Parent* bases (required)

Mirror reference hierarchy; improve Dialog/Sheet slightly for safety.

```
core/ui/base/
  activity/ParentActivity.kt
  fragment/ParentFragment.kt
  dialog/ParentDialogDismissal.kt + ParentDialog.kt
  sheet/ParentSheetDismissal.kt + ParentSheet.kt
core-ui …/extensions/
  FragmentExtensions.kt   # viewLifecycleOwner collectWhen* / launchWhen* + navigateTo / popFrom
  ActivityExtensions.kt   # Activity collectWhen* / launchWhen*
  ContextExtensions.kt    # showToast(String) / showToast(@StringRes)
  ImageViewExtensions.kt  # loadImage via Glide (ShapeableImageView / ImageView)

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
- Improvements vs fragile patterns: never access binding after destroy; use `dismissAllowingStateLoss` only via `safeDismiss`

### ParentSheet (+ Dismissal)

- `ParentSheetDismissal` : `BottomSheetDialogFragment` with `dismissCallback` + `safeShow` / `safeDismiss`
- `ParentSheet` : inflate with `bindingFactory`, null-safe `_binding` (same as Fragment — **not** `!!`), `onSheetCreated()`, `initObservers()`
- Improvements: remove unused dialog imports; optional `skipCollapsed` / expanded state in `onStart` when product needs it; keep Binding lifecycle identical to Fragment

Also add: Fragment/Activity/Context/ImageView extensions (`FragmentExtensions` / `ActivityExtensions` / `ContextExtensions.showToast` / `ImageViewExtensions.loadImage` via Glide; Fragment uses `viewLifecycleOwner`), `themes.xml` (include `ButtonStyle.IconButton` parent of `Widget.Material3.Button.IconButton`), **`splash.xml`**, `strings.xml` / `colors.xml` with **app → general → screen-wise** sections (`09-resources-xml`). Add Glide to version catalog + `implementation(libs.glide)` on `:core-ui`.

## Step 7 — `:core-platform`

### Firebase Messaging (mandatory — dependency only)

Follow **`implement-firebase-messaging`**. Every new project **must** include:

```toml
# Firebase
firebaseMessaging = "…"   # latest stable
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging", version.ref = "firebaseMessaging" }
```

```kotlin
// Firebase
implementation(libs.firebase.messaging)
```

On **`:core-platform`** only — `:app` pulls it transitively. Do **not** add `FirebaseMessagingService`, FCM manifest entries, or token handling during setup.

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

    fun Throwable.recordException(log: String) { /* Crashlytics + Log */
    }

    fun String.postFirebaseEvent() { /* Analytics bundle + TAG_FIREBASE log */
    }

    fun getDeviceToken() { /* FirebaseInstallations token log */
    }
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

- `SharedPrefManager(context)` — **sync only, no dispatcher** (see `.cursor/rules/reference/shared-preferences.md` + `26-data-persistence.mdc`)
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

- [ ] `.cursor/project-settings.json` written and valid
- [ ] Every module has `.gitignore` (`/build`; `:app` also `/release`)
- [ ] No `:app/src/main/res/values/` (themes/strings/colors live in `:core-ui`)
- [ ] Modules: app, domain, data, presentation, core-common, core-ui, core-platform
- [ ] `:app` `android` section order: defaultConfig → signingConfigs → buildTypes → buildFeatures → compileOptions → bundle
- [ ] `:app` has `signingConfigs` (`.jks` path if found, else empty strings) + `bundle.language.enableSplit = false` + `base.archivesName`
- [ ] Library modules omit `signingConfigs` / `bundle` / `base`
- [ ] UseCases + repo interfaces only in `:domain`; `dataModule` has `//// DataSources` then `//// Repositories`
- [ ] All DI uses `lazyModule` / `lazyModules` only; theme applied after `startKoin` (no `GlobalContext` probes)
- [ ] `nav_graph` startDestination = `entranceFragment`
- [ ] `:core-ui` has `anim/` + `anim-ldrtl/` slide_* set; nav actions use the four anim attrs
- [ ] ParentActivity / ParentFragment / ParentDialog / ParentSheet (+ Dismissal) exist
- [ ] `FragmentExtensions.kt` + `ActivityExtensions.kt` + `ContextExtensions.kt` + `ImageViewExtensions.kt` (`showToast` / `loadImage`; Fragment collectors on `viewLifecycleOwner`; `navigateTo` / `popFrom`)
- [ ] Glide on `:core-ui` (+ presentation if needed); all programmatic image binds use `loadImage`
- [ ] `firebase-messaging` in catalog + `implementation(libs.firebase.messaging)` on `:core-platform` (no MessagingService)
- [ ] `PlatformFirebase` is `object` without Context
- [ ] Dispatchers registered **without** `named("io")` / `named("default")`
- [ ] RC `minimumFetchIntervalInSeconds(0)` + cache write to `SharedPrefManager`
- [ ] `presentation` ↛ `:data`
- [ ] `assembleDebug` succeeds; orientation / theme modes match `project-settings.json`

## Do not

- Compose / Data Binding / Hilt
- App-level `values` resources
- Named dispatcher qualifiers
- `PlatformFirebase` holding Context
- Reading RC only from SDK in Fragments (use prefs cache)
- Hardcode secrets / lock orientation unless product requires
- Skip writing `project-settings.json`
- Add `FirebaseMessagingService` / FCM manifest / push UI during setup (dependency only)

## After setup

Next: language / onboarding / home via `figma-to-xml` → `create-mvi`; new domain/data via `create-clean-architecture`; wire Entrance Effects in `nav_graph.xml`.
