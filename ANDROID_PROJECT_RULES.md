# Android Project Engineering Rules Document

> Extracted from **Qibla Finder / Qibla Compass** (`Qibla-Finder-Qibla-Compass-HSAIAppsLab`).  
> Intended for Cursor Rules, Claude.md, and AI agent knowledge bases.  
> Follow these rules for all new features and future projects based on this architecture.

---

## Project Baseline (Detected)

| Setting | Value |
|---------|--------|
| Language | **Kotlin only** |
| UI toolkit | **XML + View Binding** (No Jetpack Compose) |
| Architecture | **Clean Architecture** |
| Presentation pattern | **MVVM + MVI** (Intent / State / Effect) |
| DI | **Koin** (`lazyModule`, `single`, `factory`, `viewModel`) |
| Build | **Gradle Kotlin DSL** + **Version Catalog** (`gradle/libs.versions.toml`) |
| minSdk | **24** |
| targetSdk | **36** |
| compileSdk | **36** (minorApiLevel = 1) |
| Java compatibility | **17** |
| Kotlin code style | **official** (`gradle.properties`) |
| Root package | `qiblacompass.prayertimes.qibladirectionfinder` |

---

# 1. Project Structure Rules

## 1.1 Module Organization

Modules are declared in `settings.gradle.kts`:

| Module | Type | Responsibility |
|--------|------|----------------|
| `:app` | Application | `App` class, Koin aggregation, FCM/notification receivers & services, wiring only |
| `:domain` | Library | Entities, repository interfaces, use cases — no UI |
| `:data` | Library | Repository implementations, DataSources, SharedPrefs, RemoteConfig, Billing, Location |
| `:presentation` | Library | Screens (Fragments/Activities), feature MVI packages, adapters, navigation graphs |
| `:core-common` | Library | Pure shared constants / events — **no Android UI / DI libs** |
| `:core-platform` | Library | Platform services (network, Firebase helpers, delay handler, location clients) |
| `:core-ui` | Library | Base UI (`Parent*`), ViewBinding lifecycle, extensions, themes, drawables, strings |
| `:gmaAds` | Library | AdMob (app-open, banner, interstitial, native) with internal clean layers |
| `:feature-prayertime` | Library | Embedded prayer-time calculation engine (`com.orbitalsonic.sonicopt`) |
| `:feature-hijricalendar` | Library | Self-contained Hijri calendar UI/engine (`com.pegasus.hijricalendar`) |

## 1.2 Feature Module Structure (Presentation)

Every screen/feature under `:presentation` follows a **feature-first** package layout:

```
presentation/<featureName>/
  di/          # *PresentationModule.kt (lazyModule + viewModel)
  intent/      # sealed class *Intent
  state/       # data class *State
  effect/      # sealed class *Effect
  viewModel/   # *ViewModel
  ui/          # Fragment / Activity / BottomSheet / Dialog
  adapter/     # optional ListAdapter / FragmentStateAdapter
  model/       # optional UI models (*UiItem)
  mapper/      # optional domain → UI mappers
  enums/       # optional feature enums
  helper/      # optional feature helpers
```

**Examples from this project:** `home`, `entrance`, `language`, `onBoarding`, `digitalCompass`, `qiblaCompass`, `prayer`, `premium`, `setting`, `exit`.

Feature folder names use **camelCase** (`digitalCompass`, `qiblaMap`, `onBoarding`).

## 1.3 Core / Common Module Responsibilities

| Module | May contain | Must NOT contain |
|--------|-------------|------------------|
| `core-common` | Constants, event keys, pure Kotlin shared types | Android UI, Koin, Fragments, ViewModels |
| `core-platform` | InternetManager, Firebase helpers, handlers, platform DI | Screen UI, feature business use cases |
| `core-ui` | Themes, colors, drawables, strings, `Parent*` bases, extensions, observers | Feature-specific business logic, data repositories |

Shared visual assets and localized strings live in **`core-ui`**, not in `presentation`.

## 1.4 Package Naming Conventions

```
qiblacompass.prayertimes.qibladirectionfinder.<layer>.<area>...
```

| Layer | Package suffix |
|-------|----------------|
| Domain | `...domain.entity` / `...domain.repository.<area>` / `...domain.usecase.<area>` |
| Data | `...data.<area>.repository` / `...data.<area>.dataSource` / `...data.di` |
| Presentation | `...presentation.<feature>.{intent\|state\|effect\|viewModel\|ui\|di}` |
| Core | `...core.common` / `...core.platform` / `...core.ui` |
| Ads | `...admobAds.<adType>.{data\|domain\|presentation}` |

**Exceptions (embedded libraries):**
- `feature-prayertime` → `com.orbitalsonic.sonicopt`
- `feature-hijricalendar` → `com.pegasus.hijricalendar`

## 1.5 Layer Separation Rules

```
UI (presentation) → Domain (interfaces + use cases) → Data (implementations)
```

- **Domain** defines contracts (`*Repository` interfaces) and use cases.
- **Data** implements those contracts (`*RepositoryImpl`) and owns DataSources.
- **Presentation** talks to **domain** (use cases / repository interfaces) — **never** imports `:data` implementations.
- **App** wires everything via Koin (`KoinModules`).

## 1.6 Dependency Direction Rules

```
feature-prayertime ──► domain ◄── data
                          ▲
                          │
                     presentation ──► gmaAds ──► data
                          │              │
                          └──► core-ui / core-platform / core-common
                          │
                          └──► feature-hijricalendar / feature-prayertime

app ──► presentation, data, domain, gmaAds, core-*
```

### Allowed

| From | To |
|------|----|
| `presentation` | `domain`, `core-*`, `gmaAds`, `feature-*` |
| `data` | `domain`, `core-*`, `feature-prayertime` |
| `domain` | `feature-prayertime`, coroutines-core only |
| `gmaAds` | `data`, `core-*` |
| `app` | all modules (composition root) |

### Forbidden Dependencies

- `presentation` → `data` ❌
- `domain` → `presentation` / `data` / `core-ui` / Android UI libs ❌
- `core-common` → Android UI / Koin ❌
- `feature-*` → app feature modules (must stay standalone) ❌
- Circular module dependencies ❌
- Hardcoded Maven versions outside `libs.versions.toml` ❌
- Jetpack Compose libraries ❌
- Hilt / Dagger (project uses Koin) ❌

---

# 2. Manifest Rules

## 2.1 AndroidManifest.xml Structure

| Module | Manifest role |
|--------|----------------|
| `:app` | Canonical merged manifest: Application, Activities, Receivers, Services, Providers, meta-data |
| `:presentation` | Permissions only (location, notifications) — no components |
| `:core-platform` | Permissions only (`INTERNET`, `ACCESS_NETWORK_STATE`) |
| `:gmaAds` | Non-exported ad Activities + `INTERNET` |
| `:feature-hijricalendar` | Empty `<application />` stub |

## 2.2 Application Class Rules

- Single Application class in `:app` (example: `App.kt`).
- Application **only** initializes DI (Koin). Keep it minimal.

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

- Declare with `android:name=".App"`.
- `allowBackup="false"` with backup/data-extraction XML rules.
- `supportsRtl="true"`.
- Launch theme uses Splash Screen theme (`Theme.App.Starting`).

## 2.3 Activity Declaration Rules

- Prefer **single-Activity** architecture (`MainActivity` as launcher).
- Launcher Activity: `android:exported="true"` + `MAIN`/`LAUNCHER` intent-filter.
- Non-launcher Activities (e.g. ad loading): `android:exported="false"`.
- Portrait lock is used in this project (`screenOrientation="portrait"`).
- Activities may live in `:presentation` or `:gmaAds`; declare fully-qualified class names when outside app package.

## 2.4 Fragment Declaration Rules

- Fragments are **not** declared in the manifest.
- Hosted via Navigation Component (`FragmentContainerView` + `nav_graph.xml` / `nav_graph_dashboard.xml`).
- Use Safe Args plugin for typed navigation arguments.

## 2.5 Service Rules

- Declare only in `:app` manifest.
- Default: `android:exported="false"`.
- Foreground services must set `foregroundServiceType` (example: `mediaPlayback` for Azan).
- Matching permissions required (`FOREGROUND_SERVICE`, type-specific permission).

## 2.6 BroadcastReceiver Rules

- Custom app actions: `exported="false"` (example: prayer alarm receiver).
- System broadcasts that must be received externally: `exported="true"` (example: `BOOT_COMPLETED`, timezone/date changes).
- Prefer explicit package-scoped action names:  
  `qiblacompass.prayertimes.qibladirectionfinder.ACTION_PRAYER_ALARM`

## 2.7 Provider Rules

- Prefer `exported="false"`.
- Use `tools:node="merge"` / `tools:node="remove"` carefully for AndroidX Startup.
- This project removes `WorkManagerInitializer` to avoid startup crashes.

## 2.8 Permission Handling

- Declare permissions closest to the module that needs them; app merges them.
- Dangerous permissions (location, camera, notifications) must be requested at runtime via `BasePermissionFragment` / effects — never assume granted.
- Optional hardware: `uses-feature ... required="false"` (camera).

## 2.9 Exported Attribute Usage

| Component | Rule |
|-----------|------|
| Launcher Activity | `exported="true"` |
| Other Activities | `exported="false"` |
| Custom receivers | `exported="false"` |
| System receivers | `exported="true"` when required |
| Services | `exported="false"` |
| Providers | `exported="false"` |

**Never leave `exported` unspecified** for components with intent-filters.

## 2.10 Deep Links

- This project currently has **no app deep links**.
- If adding deep links: declare on the host Activity with explicit `intent-filter`, `autoVerify` only when App Links are configured, and handle navigation via Nav Component.

## 2.11 Intent Filters

- Launcher: `MAIN` + `LAUNCHER` only on MainActivity.
- Receivers: whitelist only needed system actions.
- Custom actions must be namespaced with applicationId / package.

## 2.12 Theme Configuration

- Application theme: Splash → post-splash Material3 DayNight theme.
- Activity may override with splash theme for cold start.
- Themes live in `core-ui` (`Theme.Material3.DayNight.NoActionBar` parent).

## 2.13 Manifest Placeholders

| Placeholder | Source | Usage |
|-------------|--------|--------|
| `${MAPS_API_KEY}` | `local.properties` via `app/build.gradle.kts` | Google Maps meta-data |
| `${applicationId}` | AGP | Startup provider authority |

Rules:
- Secrets (API keys) go in `local.properties` / CI secrets — **not** committed as hardcoded production secrets in source.
- AdMob IDs use `resValue` per buildType in `:gmaAds` (debug uses Google sample IDs).

---

# 3. Kotlin Code Rules

## 3.1 General Kotlin Rules

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `HomeViewModel`, `LanguageRepositoryImpl` |
| Functions | camelCase | `handleIntent`, `getLastLocation` |
| Properties / locals | camelCase | `currentCity`, `isLoading` |
| Constants | UPPER_SNAKE / object members | `Constants.TAG` |
| Packages | lowercase / camelCase feature folders | `presentation.home.viewModel` |
| Boolean | `is` / `has` / `should` / `show` prefix | `isLoading`, `showPremiumIcon` |

### File Naming

- One primary public type per file; filename matches type: `HomeViewModel.kt`.
- Extension files: `<Receiver>Extensions.kt` (e.g. `FragmentExtensions.kt`).
- DI modules: `<Feature>PresentationModule.kt` or area modules in `DataModule.kt`.

### Extension Function Usage

- Place shared UI extensions in `core-ui/.../extensions/`.
- Prefer extensions for lifecycle-safe Flow collection, navigation, toasts, Glide, View helpers.
- Do not put business logic in extensions.

### Null Safety

- Prefer nullable types with explicit handling over `!!`.
- UI state uses nullable fields + computed `showX` helpers (see `HomeState`).
- Binding access: guarded; throw clear `IllegalStateException` outside view lifecycle (`ParentFragment`).

### Data Class Usage

- UI state: `data class *State(...)`.
- Domain entities: `data class` in `domain.entity`.
- UI list models: `data class *UiItem`.
- Prefer immutable `val` properties; update via `copy` / `_state.update { it.copy(...) }`.

### Sealed Class Usage

- User/system actions: `sealed class *Intent`.
- One-shot side effects: `sealed class *Effect`.
- Domain destinations / closed hierarchies: `sealed class` (e.g. entrance destination).
- Prefer `object` for parameterless branches; `data class` when payload is needed.

### Object Usage

- Stateless mappers: `object LanguageUiMapper`.
- Singletons only when truly global; otherwise prefer Koin `single`.

### Enum Usage

- Closed fixed sets: prayer conventions, ad keys, tabs, orientation.
- Prefer enums over stringly-typed constants for domain/UI options.

### Scope Functions

- Use `apply`/`also` for configuration; `let` for null-safe transforms; `with`/`run` sparingly.
- Prefer `_state.update { }` over nested `apply` for state mutation.
- Avoid deep nesting of scope functions.

---

## 3.2 Architecture Rules

### ViewModel Responsibilities

ViewModels **must**:
- Extend `androidx.lifecycle.ViewModel` (no custom BaseViewModel in this project).
- Expose `StateFlow<*State>` and `SharedFlow<*Effect>`.
- Accept a single entry: `fun handleIntent(intent: *Intent)`.
- Call use cases / repository interfaces — not Android Views.
- Use `viewModelScope` + `CoroutineExceptionHandler`.

ViewModels **must not**:
- Hold View / Fragment / Context references (except carefully injected app-level managers already used by the project).
- Navigate directly; emit Effects instead.
- Perform layout inflation or View Binding.

### State Management Approach

```kotlin
private val _state = MutableStateFlow(HomeState())
val state: StateFlow<HomeState> = _state.asStateFlow()

_state.update { it.copy(isLoading = true) }
```

- Continuous UI data → **StateFlow**.
- Default state values required on the data class.
- Derived UI flags as computed properties on State (`showCity`, `showTime`, …).

### Intent / Event Handling

```kotlin
fun handleIntent(intent: HomeIntent) = viewModelScope.launch(coroutineExceptionHandler) {
    when (intent) {
        HomeIntent.FindCurrentLocation -> findCurrentLocation()
        is HomeIntent.PermissionResult -> onPermissionResult(intent.granted)
    }
}
```

- UI events map 1:1 to Intent sealed subtypes.
- Fragments call `viewModel.handleIntent(...)` only — no business branching in UI beyond rendering.

### UI State Classes

```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val currentCity: String? = null,
) {
    val showCity: Boolean = currentCity != null
}
```

- Name: `<Feature>State`.
- Package: `.../<feature>/state/`.
- Keep ProGuard keep rules for `presentation.**.state.**`.

### Effect Classes

```kotlin
sealed class HomeEffect {
    object NavigateToPrayerTime : HomeEffect()
    data class ShowErrorRes(@StringRes val messageResId: Int) : HomeEffect()
}
```

- One-shot: navigation, permissions, toasts, sheets.
- Prefer `@StringRes` for user-facing messages when possible.

### UseCase Rules

- Live in `:domain` under `usecase/<area>/`.
- Naming: `Get*|Set*|Save*|Schedule*|Cancel*|Check*UseCase`.
- Depend only on repository **interfaces**.
- Registered in Koin as **`factory`** (`useCaseModule`).
- One primary responsibility per use case class (may expose a few related suspend functions).

```kotlin
class GetLocationAndAddressUseCase(private val repository: LocationRepository) {
    suspend fun getLastLocation(): GeoLocation? = repository.getLastLocation()
    suspend fun getAddressForLocation(location: GeoLocation): LocationAddress? =
        repository.getAddressForLocation(location)
}
```

### Repository Rules

| Layer | Type | Naming |
|-------|------|--------|
| Domain | `interface` | `LocationRepository` |
| Data | `class` | `LocationRepositoryImpl` |

- Bind in Koin: `single<XRepository> { XRepositoryImpl(...) }`.
- Domain interfaces describe capability; data owns Android SDK / network / prefs.
- Prefer suspend / Flow returns over callbacks.

### DataSource Rules

- Local/remote sources under `data/<area>/dataSource/` (or `dataSources/`).
- Naming: `LanguageDataSource`, `RemoteConfigDataSource`, `SharedPrefManager`.
- gmaAds pattern: `DataSourceLocalX` / `DataSourceRemoteX`.
- Registered as Koin `single`.
- Repositories orchestrate DataSources; UI never talks to DataSources.

### Mapper Rules

- Prefer explicit mappers for domain → UI (`LanguageUiMapper.toUi(...)`).
- Place under feature `mapper/` package when non-trivial.
- Keep mapping out of XML; do it in Kotlin.
- Avoid leaking Android resource IDs into domain entities.

### UI / Fragment Rules

Hierarchy:

```
ParentFragment (core-ui, ViewBinding lifecycle)
  └── BasePermissionFragment (runtime permissions)
        └── BaseFragment (ads, billing, shared prefs injects)
              └── Feature Fragment (MVI collect + render)
```

- Collect state with `collectWhenStarted`.
- Collect effects with `collectWhenCreated`.
- Implement `initObservers()` and `onViewCreated()`.

---

## 3.3 SOLID Rules (How This Project Applies Them)

### Single Responsibility (S)
- UseCase = one business capability area.
- ViewModel = state reduction + intent handling for one screen.
- Repository = one data concern (location, billing, language, …).
- Modules split UI / domain / data / ads / features.

### Open / Closed (O)
- New screens add a new feature package + Koin module — existing modules stay closed.
- Sealed Intent/Effect hierarchies extend by adding subtypes.
- Ad keys/enums extend without rewriting managers.

### Liskov Substitution (L)
- Any `*RepositoryImpl` must honor the domain interface contract (nullability, suspend semantics).
- Fragments substituting `BaseFragment` must preserve ViewBinding lifecycle rules.

### Interface Segregation (I)
- Narrow repository interfaces per area (`LanguageRepository`, `LocationRepository`) instead of a god repository.
- Feature Intents expose only that screen’s events.

### Dependency Inversion (D)
- Presentation/domain depend on abstractions (`LocationRepository`), not `LocationRepositoryImpl`.
- Koin composition root in `:app` binds interfaces → implementations.

---

## 3.4 Coroutine Rules

### Coroutine Scope Usage

| Scope | Use |
|-------|-----|
| `viewModelScope` | All ViewModel work |
| `lifecycleScope` + `repeatOnLifecycle` | UI collection via `collectWhenStarted/Created` |
| Do not use `GlobalScope` | Forbidden for feature work |

### Dispatcher Handling

- Inject `Dispatchers.IO` / `Dispatchers.Default` via Koin when needed.
- Data layer: `withContext(Dispatchers.IO)` for geocoding / disk / network.
- UI updates happen by collecting on main (lifecycle collectors).

### Flow Usage

- Prefer cold `Flow` for streams from repositories when continuous observation is needed.
- Use operators (`map`, `catch`, `flowOn`) in data/presentation as appropriate.
- Play Services Tasks: use `await()` via `kotlinx-coroutines-play-services`.

### StateFlow Rules

- Private mutable + public immutable: `_state` / `state`.
- Update with `_state.update { it.copy(...) }`.
- Initial value always provided.

### SharedFlow Rules

- Effects: `_effect` as `MutableSharedFlow`, exposed as `SharedFlow`.
- Emit navigation / one-shot events; do not put continuous UI data in SharedFlow.

### Exception Handling

```kotlin
private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
    viewModelScope.launch { handleError(exception) }
}

fun handleIntent(intent: HomeIntent) = viewModelScope.launch(coroutineExceptionHandler) { ... }
```

- Convert failures to Effect (`ShowError` / `ShowErrorRes`) or state error flags.
- Log with shared `Constants.TAG`.
- Never swallow exceptions silently.

---

## 3.5 Dependency Injection Rules

### DI Framework

- **Koin only** (`koin-android`, `koin-core-coroutines`).
- Bootstrap in `App` with `lazyModules(...)`.
- Aggregate list in `app/.../di/KoinModules.kt`.

### Module Organization

| Location | Contents |
|----------|----------|
| `app/di/modules/AppModule.kt` | App-level singles |
| `app/di/modules/UseCaseModule.kt` | Domain use case factories |
| `data/di/DataModule.kt` | Repository + DataSource singles (split vals) |
| `core-ui/.../CoreModule.kt` | Core UI deps |
| `core-platform/.../CorePlatformModule.kt` | Platform deps |
| `gmaAds/.../GMAAdsModule.kt` | Ad modules |
| `presentation/<feature>/di/*PresentationModule.kt` | Feature ViewModels |

### Naming Conventions

- Vals: `homePresentationModule`, `languagesModule`, `useCaseModule`, `bannerAdModule`.
- Always declare with `lazyModule { }`.
- After adding a module, **register it in `KoinModules.getKoinModules()`**.

### Singleton Rules (`single`)

Use for:
- Repositories and DataSources
- Managers (`BillingManager`, `PrayerTimeManager`, `AppOpenAdManager`)
- Shared configs / dispatchers

### Factory Rules (`factory`)

Use for:
- UseCases (stateless, cheap to recreate)

### ViewModel Rules (`viewModel`)

```kotlin
val homePresentationModule = lazyModule {
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
}
```

### Named Qualifiers

- Parallel instances (multiple banners / interstitial configs) use `named("...")`.
- Keep qualifier strings consistent across declaration and injection sites.

### Service Locator

- `DIComponent` in core-ui exists for limited shared access; prefer constructor injection / `by inject()` in bases when following existing patterns.

---

# 4. Resource Rules

## 4.1 XML Layout

### Naming Convention

| Prefix | Usage | Example |
|--------|--------|---------|
| `fragment_` | Screens | `fragment_home.xml` |
| `activity_` | Activities | `activity_main.xml` |
| `layout_` | Includes / reusable blocks | `layout_dashboard_content_prayer.xml` |
| `item_` | RecyclerView rows | `item_language.xml` |
| `dialog_` | Dialogs | `dialog_prayer_notification_information.xml` |
| `bottom_sheet_` | Bottom sheets | `bottom_sheet_compass_guidelines.xml` |
| `view_` | Custom view roots | `view_hijri_calendar.xml` |

Use snake_case. Feature screens: `fragment_{feature}_{subfeature?}`.

### Folder Structure

| Resources | Module |
|-----------|--------|
| Screen layouts | `:presentation` |
| Shared drawables, themes, strings, colors | `:core-ui` |
| Ad layouts | `:gmaAds` |
| Calendar layouts | `:feature-hijricalendar` |

### View ID Naming (Hungarian + camelCase)

| Prefix | Widget |
|--------|--------|
| `mb` | MaterialButton |
| `mtv` | MaterialTextView |
| `siv` | ShapeableImageView |
| `mcv` | MaterialCardView |
| `cl` | ConstraintLayout |
| `ll` | LinearLayout |
| `fl` | FrameLayout |
| `fcv` | FragmentContainerView |
| `bnv` | BottomNavigationView |
| `rcv` | RecyclerView |
| `vp` | ViewPager2 |
| `lav` | LottieAnimationView |
| `shimmer` | ShimmerFrameLayout |

Pattern: `{prefix}{Role}{ScreenOrItemContext}`  
Examples: `mtvHeadingHome`, `mbGuidelineHome`, `sivFlagItemLanguage`, `clRootHome`.

### Component Reuse

- Shared styles in `core-ui` themes (`TextStyle.*`, `ButtonStyle.*`).
- Include reusable `layout_*` files.
- Reuse item layouts across adapters when identical (`item_language` used by language + settings).

### ConstraintLayout Rules

- **Primary** layout engine for screens and most items.
- Scrollable screens: `NestedScrollView` / `ScrollView` wrapping a `ConstraintLayout`.
- Prefer constraints over nested weight-heavy LinearLayouts for complex UIs.
- Use Material components (`MaterialButton`, `MaterialTextView`, `MaterialCardView`, `ShapeableImageView`).

### RecyclerView / Adapter Rules

- Prefer `ListAdapter<UiItem, VH>` + `DiffUtil.ItemCallback`.
- Inflate with View Binding.
- Submit lists from Fragment after mapping State → UI models.
- ViewPager2: `FragmentStateAdapter` for tabbed flows (`OnBoardingPagerAdapter`, `DigitalCompassPagerAdapter`).
- Optional item animation resource: `item_anim_fade_slide`.

---

## 4.2 Drawable Rules

### Naming Convention

| Prefix | Role | Example |
|--------|------|---------|
| `ic_svg_` | Vector icons | `ic_svg_back.xml` |
| `ic_png_` | Raster icons | `ic_png_home_clock.webp` |
| `ic_shape_` | Decorative shapes | `ic_shape_home_line_selected.xml` |
| `img_svg_` | Large vectors | `img_svg_qibla_compass_1.xml` |
| `img_png_` | Large rasters | `img_png_premium.webp` |
| `bg_` | Backgrounds / containers | `bg_container_selected.xml` |
| `fg_` | Foreground gradients | `fg_gradient_prayer_time.xml` |
| `flag_` | Language flags | `flag_ar.xml` |

### Shape Drawable Rules

- Prefer XML shape/selector drawables for containers and states.
- Name selected/unselected pairs explicitly (`bg_container_selected` / `bg_container_unselected`).

### Vector Drawable Rules

- Prefer vectors (`ic_svg_*`) for icons.
- Night-specific assets go in `drawable-night/` when needed (gradients, onboarding images).

---

## 4.3 String Rules

### Naming

| Pattern | Example |
|---------|---------|
| `toast_*` | `toast_no_internet_connection` |
| `action_*` | `action_continue` |
| `{screen}_title` / `_body` | `language_body` |
| `{feature}_*` | `compass_guideline_1` |
| `premium_*` / `setting_*` / `exit_*` | `premium_title` |
| Numbered variants | `_one`/`_two` or `_1`/`_2` |

Group with XML comment section headers.

### Localization

- Default: `core-ui/src/main/res/values/strings.xml`.
- Translations: `values-ar`, `values-de`, `values-es`, `values-fr`, `values-hi`, `values-tr`, `values-ur`.
- Non-translatable config (emails, URLs): `translatable="false"` (e.g. `app_info.xml`).
- Bundle language splits disabled (`bundle.language.enableSplit = false`) — ship all languages in one APK.

### Hardcoded Text Restrictions

- **No hardcoded user-facing strings in Kotlin/XML layouts** — use `@string/` / `R.string`.
- Effects should prefer `@StringRes` where possible.
- Debug/log messages may be hardcoded English.

---

## 4.4 Color Rules

- Material 3 tokens: `md_theme_*` in `colors.xml` + `values-night/colors.xml`.
- Semantic colors: `bodyTextColor`, `colorPrimary`, premium gradient attrs.
- Prefer theme attributes in layouts (`?attr/colorSurface`, `?attr/colorOnBackground`).
- Custom premium attrs declared in `attrs.xml`.

### Material 3 Usage

- Parent theme: `Theme.Material3.DayNight.NoActionBar`.
- Use Material3 widget style parents for buttons/radios.
- DayNight automatic light/dark via `values` + `values-night`.

---

## 4.5 Dimension Rules

- This project currently uses **inline dp/sp** in layouts/styles (no shared `dimens.xml`).
- Common spacing observed: `8dp`, `12dp`, `16dp`.
- Text sizes via theme text styles: ~`12sp`–`28sp`.
- **Rule for future work:** prefer theme text/button styles over one-off sizes; if introducing `dimens.xml`, keep it in `core-ui` and reuse names consistently.
- Use `dp` for layout, `sp` for text.

---

## 4.6 Theme Rules

- Base theme in `core-ui` with full M3 color role mapping.
- App theme extends base; splash theme uses AndroidX SplashScreen API and `postSplashScreenTheme`.
- Define typography (`TextStyle.Title.*`, `TextStyle.Body.*`) and button styles (`ButtonStyle.Primary/Secondary/Outlined/Text/Icon`).
- Font: Roboto family via `core-ui` font resources + `preloaded_fonts`.
- Light/dark: rely on DayNight + `values-night` overrides — do not fork entire layouts for dark mode unless necessary.

---

# 5. Gradle Rules

## 5.1 Gradle Structure

### Root `build.gradle.kts`

- Only declares plugins with `apply false` via version catalog aliases.
- No dependency declarations at root.

### Module `build.gradle.kts`

Every Android module:
- Uses `alias(libs.plugins.android.application|library)`.
- Sets `namespace`, `compileSdk` 36, `minSdk` 24.
- Java 17 `compileOptions`.
- Library modules: `isMinifyEnabled = false`; app release enables minify/shrink.

### Plugin Management

- Repositories filtered in `settings.gradle.kts` (`pluginManagement`).
- `dependencyResolutionManagement.repositoriesMode = FAIL_ON_PROJECT_REPOS`.
- Extra repos: Google, Maven Central, JitPack, mediation repos as needed.

### Convention Plugins

- This project does **not** currently use custom `build-logic` convention plugins.
- Keep module scripts consistent manually; if introducing convention plugins later, put shared Android/Kotlin config there without breaking catalog usage.

## 5.2 Version Management

**Mandatory:**
- All versions in `gradle/libs.versions.toml`.
- Dependencies referenced as `libs.*`.
- Plugins referenced as `libs.plugins.*`.
- **No hardcoded dependency/plugin versions** in module scripts.

Sections in catalog:
- `[versions]`
- `[libraries]`
- `[plugins]`

## 5.3 Dependency Rules

### Declaration Style

```kotlin
implementation(project(":domain"))
implementation(libs.androidx.core.ktx)
api(libs.google.mobile.ads) // only when consumers must see the type
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.espresso.core)
coreLibraryDesugaring(libs.desugar.jdk.libs)
```

### `implementation` vs `api`

| Use | When |
|-----|------|
| `implementation` | Default for almost all deps |
| `api` | Rare — when module types must be exposed to consumers (gmaAds exposes Mobile Ads / Meta mediation) |

### Test Dependencies

- Unit: `junit`
- Instrumentation: `androidx-junit`, `espresso-core`
- Declared at least on `:app` today; add per-module as tests are introduced.

### Debug Dependencies

- Prefer buildType `resValue` / `applicationIdSuffix` over separate debug-only dependency trees unless necessary.
- Debug applicationId suffix: `.testing`.

## 5.4 Build Configuration

### BuildTypes

| Type | Rules |
|------|-------|
| `debug` | minify off; appId suffix `.testing`; sample AdMob IDs in gmaAds |
| `release` | minify + shrink resources on **app**; release signing; production AdMob IDs |

### ProductFlavors

- Not used currently. Do not add flavors unless product requirements demand it.

### Signing Configuration

- Release signing configured in `app/build.gradle.kts`.
- **Future rule:** move keystore passwords to env/`local.properties` — do not duplicate secrets in docs or commits when refactoring.

### ProGuard / R8

- `android.enableR8.fullMode=true` in `gradle.properties`.
- Meaningful keep rules live in **`app/proguard-rules.pro`** only.
- Keep domain entities + presentation `state`/`intent`/`effect`/`model` packages + ads entities.
- Preserve SourceFile/LineNumberTable for Crashlytics.
- Library module `proguard-rules.pro` files are stubs unless module-specific keeps are required.

### Other Build Features

- View Binding: **enabled** on UI modules (`app`, `presentation`, `core-ui`, `gmaAds`, `feature-hijricalendar`).
- BuildConfig: enabled where needed.
- Core library desugaring: enabled for `java.time` on older APIs (`app`, `feature-hijricalendar`).
- Navigation Safe Args: enabled on `presentation`.
- Parcelize: enabled where Parcelable models exist (`data`, `presentation`).

---

# 6. Dependency Rules

## 6.1 Mandatory Libraries (Established Stack)

| Category | Library | Catalog alias |
|----------|---------|---------------|
| AndroidX Core | core-ktx, appcompat, activity, fragment-ktx | `androidx-*` |
| UI | Material 3, ConstraintLayout | `material`, `androidx-constraintlayout` |
| Navigation | navigation-fragment/ui + Safe Args | `androidx-navigation-*` |
| Lifecycle | viewmodel/runtime/process | `androidx-lifecycle-*` |
| DI | Koin Android | `koin-android`, `koin-core-coroutines` |
| Coroutines | kotlinx-coroutines-core (+ play-services) | `kotlinx-coroutines-*` |
| Splash | core-splashscreen | `androidx-core-splashscreen` |
| Images | Glide | `glide` |
| Desugaring | desugar_jdk_libs | `desugar-jdk-libs` |

## 6.2 Optional Libraries (Allowed When Feature Needs Them)

| Library | When to use |
|---------|-------------|
| Lottie | Complex animations already patterned in UI |
| Dots Indicator | Onboarding / pager indicators |
| Shimmer | Loading placeholders (ads/premium) |
| CameraX | Camera-based features (digital compass) |
| Play Services Maps / Location | Map & GPS features |
| Firebase Crashlytics / Analytics / Messaging / Remote Config | Existing platform integrations |
| Play Billing wrapper (`google-billing`) | Premium IAP |
| Google Mobile Ads + Meta mediation | Ads via `:gmaAds` |
| App Update KTX | In-app updates |

## 6.3 Forbidden Libraries (Unless Explicitly Approved)

- Jetpack **Compose** (UI is XML-only)
- **Hilt / Dagger** (Koin is the DI standard)
- Alternative navigation libs replacing Navigation Component
- Alternative image loaders (use Glide) without approval
- RxJava (coroutines/Flow are standard)
- Random new networking stacks if not already present
- Unused mediation adapters (keep commented catalog entries disabled until approved)

## 6.4 Replacement Recommendations

| Do not introduce | Use instead |
|------------------|-------------|
| Compose UI | XML + View Binding + Material3 |
| Hilt | Koin `lazyModule` |
| LiveData for new MVI screens | StateFlow + SharedFlow |
| Manual findViewById | View Binding |
| Hardcoded versions | `libs.versions.toml` |
| God `Utils` classes | Focused extensions / use cases / repositories |

---

# 7. Testing Rules

## 7.1 Current State

- Only Android Studio template tests exist (`ExampleUnitTest`, `ExampleInstrumentedTest`).
- **New features should add real tests** going forward.

## 7.2 Unit Testing Approach

- Prefer pure JVM unit tests for:
  - UseCases (fake repositories)
  - Mappers
  - State reduction helpers
- Place tests under `src/test/java` mirroring package structure.

## 7.3 ViewModel Testing

- Drive with Intents; assert StateFlow emissions and SharedFlow effects.
- Use Turbine or similar only if approved and added to the catalog.
- Inject fakes instead of real Android dependencies.

## 7.4 Repository Testing

- Fake DataSources for local logic.
- Mock/fake location/billing boundaries.
- Verify mapping and error paths.

## 7.5 Fake Implementations

- Create `Fake*Repository` implementing domain interfaces in `src/test`.
- Prefer fakes over heavy mocking for repositories/use cases.

## 7.6 Mocking Framework

- Not established in catalog yet.
- If introducing mocks, add via version catalog (e.g. MockK) after approval — do not hardcode versions.

## 7.7 Test Naming Conventions

```
`<Unit>_when_<condition>_then_<result>`
```

Examples:
- `GetLocationAndAddressUseCase_whenRepositoryReturnsNull_thenReturnsNull`
- `HomeViewModel_whenPermissionDenied_thenEmitsShowError`

Instrumentation tests: describe user-visible behavior.

---

# 8. Naming Convention Rules

| Kind | Rule | Example |
|------|------|---------|
| Classes | PascalCase, role suffix | `HomeFragment`, `BillingRepositoryImpl` |
| Functions | camelCase, verb-first | `handleIntent`, `getAddressForLocation` |
| Variables | camelCase | `currentCity`, `_state` (private backing) |
| Packages | app id + layer + feature | `...presentation.home.viewModel` |
| Resources | snake_case + prefix | `fragment_home`, `ic_svg_back`, `toast_no_internet` |
| Modules | kebab-case Gradle names | `:core-ui`, `:feature-prayertime` |
| Interfaces | No `I` prefix; plain capability name | `LocationRepository` |
| Implementations | `Impl` suffix | `LocationRepositoryImpl` |
| ViewModels | `*ViewModel` | `HomeViewModel` |
| Intents/States/Effects | `*Intent` / `*State` / `*Effect` | `HomeIntent` |
| UseCases | `*UseCase` | `GetTodayPrayerTimesUseCase` |
| DI modules | `*Module` val | `homePresentationModule` |
| Ad layer (gmaAds) | Often `ViewModelX`, `UseCaseX`, `RepositoryX` | `ViewModelBanner` |

---

# 9. Android Version Compatibility Rules

## 9.1 Minimum / Target API

- **minSdk = 24**
- **targetSdk = 36**
- **compileSdk = 36**
- Do not call APIs below without compatibility guards / desugaring / AndroidX.

## 9.2 Permission Handling Strategy

- Declare in manifests; request at runtime for dangerous permissions.
- Route permission UX through Intents → Effects → `BasePermissionFragment` helpers.
- Handle permanently denied / GPS-off cases via Effects (as Home does).
- `POST_NOTIFICATIONS` required for API 33+.

## 9.3 Deprecated API Replacements

| Prefer | Over |
|--------|------|
| AndroidX libraries | Support libraries |
| SplashScreen API | Legacy splash themes alone |
| FusedLocationProvider + coroutines await | Legacy location loops |
| View Binding | Kotlin synthetics / findViewById |
| `AppLocalesMetadataHolderService` / AppCompat locales | Manual pre-33 locale hacks without AndroidX |
| Navigation Component | manual FragmentTransactions for primary app flow |

## 9.4 Backward Compatibility Rules

- Enable **core library desugaring** when using `java.time`.
- Optional hardware features: `required="false"`.
- Use `tools:ignore` only with justification (as existing portrait lock).
- Keep Apache HTTP legacy library declaration only if a dependency still needs it.
- RTL: `supportsRtl="true"`; provide `anim-ldrtl` where animations flip.

---

# 10. AI Development Rules

Rules for Cursor / Claude / coding agents generating code in this repository or clones of this architecture:

## 10.1 Always

1. **Follow existing Clean Architecture + MVVM/MVI** — Intent / State / Effect / ViewModel / UseCase / Repository.
2. **XML + View Binding only** — never introduce Compose.
3. **Use Koin** — `lazyModule`, register in `KoinModules`.
4. **Use Version Catalog** — no hardcoded dependency versions.
5. **Reuse** `Parent*` / `Base*` classes, core-ui extensions, themes, drawables, string patterns.
6. **Respect module boundaries** — presentation must not depend on `:data`.
7. **Match naming conventions** (Hungarian view IDs, resource prefixes, `*Impl`, feature package layout).
8. **Keep minSdk 24 / target-compile 36 / Java 17** unless project-wide change is requested.
9. **Put user-facing text in string resources** and update localizations when adding copy.
10. **Create tests with new features** (UseCase + ViewModel at minimum).

## 10.2 Never

1. Never introduce new libraries without explicit human approval.
2. Never change module dependency graph / boundaries casually.
3. Never add Hilt/Dagger/Compose/RxJava.
4. Never put business logic in Fragments/Activities beyond render + intent dispatch.
5. Never access DataSources from presentation.
6. Never hardcode AdMob production IDs in debug or commit secrets into git docs.
7. Never skip `exported` on manifest components with intent-filters.
8. Never use `GlobalScope` for feature work.
9. Never invent a parallel architecture (e.g. Redux, Orbit) for one screen.
10. Never modify unrelated files when implementing a feature.

## 10.3 Feature Checklist (AI / Developer)

When adding a new screen/feature:

- [ ] Create `presentation/<feature>/{di,intent,state,effect,viewModel,ui}`
- [ ] Add layout `fragment_<feature>.xml` with Hungarian IDs
- [ ] Extend appropriate `BaseFragment` / `BaseActivity` / sheet/dialog base
- [ ] Wire Navigation graph + Safe Args if needed
- [ ] Add domain UseCase(s) + repository methods if new business capability
- [ ] Implement `*RepositoryImpl` + DataSource in `:data` if needed
- [ ] Register Koin: data singles, use case factories, presentation `viewModel`
- [ ] Append modules to `KoinModules.getKoinModules()`
- [ ] Add strings to `core-ui` (+ translations)
- [ ] Use existing drawables/styles or add prefixed assets to `core-ui`
- [ ] Keep ProGuard packages covered (`state`/`intent`/`effect`/`model`)
- [ ] Add unit tests for UseCase/ViewModel
- [ ] Verify no `:presentation` → `:data` dependency

## 10.4 Code Generation Templates

### Intent
```kotlin
sealed class FeatureIntent {
    object Initialize : FeatureIntent()
    data class OnItemClicked(val id: String) : FeatureIntent()
}
```

### State
```kotlin
data class FeatureState(
    val isLoading: Boolean = false,
    val title: String? = null,
) {
    val showTitle: Boolean = title != null
}
```

### Effect
```kotlin
sealed class FeatureEffect {
    object NavigateBack : FeatureEffect()
    data class ShowErrorRes(@StringRes val messageResId: Int) : FeatureEffect()
}
```

### ViewModel skeleton
```kotlin
class FeatureViewModel(
    private val someUseCase: SomeUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FeatureState())
    val state: StateFlow<FeatureState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<FeatureEffect>()
    val effect: SharedFlow<FeatureEffect> = _effect.asSharedFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        viewModelScope.launch { /* map to effect */ }
    }

    fun handleIntent(intent: FeatureIntent) = viewModelScope.launch(coroutineExceptionHandler) {
        when (intent) {
            FeatureIntent.Initialize -> Unit
            is FeatureIntent.OnItemClicked -> Unit
        }
    }
}
```

### Presentation DI
```kotlin
val featurePresentationModule = lazyModule {
    viewModel { FeatureViewModel(get()) }
}
```

### Repository binding
```kotlin
val featureDataModule = lazyModule {
    single { FeatureDataSource() }
    single<FeatureRepository> { FeatureRepositoryImpl(get()) }
}
```

---

## Quick Reference: Golden Dependency Rule

```
app (composition root / Koin)
 └─ presentation → domain ← data
                 ↘ core-ui / core-platform / core-common
                 ↘ gmaAds / feature-*
```

**UI never imports data implementations. Domain never imports UI.**

---

*Document generated from codebase analysis. Prefer this file as the source of engineering truth for AI-assisted and human development on projects that clone this architecture.*
