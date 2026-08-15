---
name: implement-firebase-remote-config
description: First-time Firebase Remote Config with SharedPreferences cache, FetchRemoteConfigUseCase, and EntranceFragment fetch. Use when adding Remote Config, RC cache, fetchAndActivate, or /implement-firebase-remote-config — not for analytics events.
---

# Implement Firebase Remote Config (first time)

Follow `.cursor/rules/22-platform-firebase.mdc`, `26-data-persistence.mdc`, `08-gradle.mdc`, `07-dependency-injection.mdc`, `16-logging.mdc`, `00-global.mdc`.  
SharedPrefs shape: [`.cursor/rules/reference/shared-preferences.md`](../../rules/reference/shared-preferences.md).  
Setup overview: `setup-new-project` Step 8.  
**Requires human approval** before adding `firebase-config` if it is not in the catalog.

Obey `.cursor/project-settings.json` when present (`writeTestsWithFeatures`).

## Goal

Complete RC stack (Qibla pattern + domain UseCase):

1. Fetch/activate (`minimumFetchIntervalInSeconds(0)`)
2. Cache every needed key into `SharedPrefManager`
3. Runtime reads from **prefs cache**, never RC SDK in Fragments
4. Invoke from **EntranceFragment** (non-blocking)

## Entry

| App state                                                            | Action                                                                                                             |
|----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| DataSource + Repository + prefs cache + Entrance fetch already wired | Stop. Report what exists.                                                                                          |
| Stubs from `setup-new-project`                                       | Complete missing pieces only.                                                                                      |
| No RC                                                                | Continue.                                                                                                          |
| Legacy helper (`RemoteConfiguration` in Application)                 | Do **not** rewrite unless the user approves. Offer to add cache + Entrance fetch into the existing helper instead. |

Single-module: same class names, existing packages. Do **not** create `:data` / `:domain` modules without approval.

---

## Step 0 — RC keys (mandatory AskQuestion)

Audit ads / premium / feature flags. Propose a table (Firebase key, type, pref property). Do **not** copy Qibla compass-specific keys.

**AskQuestion:** confirm / add / drop keys. Wait. Then code.

Typical keys (only if the app has that surface): `appOpen`, `banner*`, `inter*`, `native*`, `counter*`, `showPremiumFirstTime`.

---

## Step 1 — Catalog + modules

Latest stable, `# Firebase` / `// Firebase` sections (`gradle-organize`).

```toml
firebaseConfig = "…"   # latest stable
firebase-config = { group = "com.google.firebase", name = "firebase-config", version.ref = "firebaseConfig" }
```

```kotlin
// Firebase
implementation(libs.firebase.config)   // :data (or :app if single-module)
```

`:app` still needs `google-services` + `google-services.json`. This skill does not add FCM.

If `InternetManager` is missing, add [templates/InternetManager.kt](templates/InternetManager.kt) to `:core-platform` `network/` (or existing network package).

Ensure `:core-common` `Constants` has `TAG` and `TAG_REMOTE_CONFIG` (`16-logging`).

---

## Step 2 — SharedPref cache properties

On `SharedPrefManager` (create per `shared-preferences.md` if missing):

```kotlin
val appOpen = "appOpen"   // Firebase RC key AND prefs key

var rcAppOpen: Int
get() = sharedPreferences.getInt(appOpen, 0)
set(value) = sharedPreferences.edit { putInt(appOpen, value) }
```

Repeat per confirmed key (`getBoolean` / `getString` when type is not int). DataSource is **sync**, no dispatcher.

---

## Step 3 — Data + domain

Copy templates; set package to `applicationId`. Paths (multi-module):

| File                                                                               | Module                               |
|------------------------------------------------------------------------------------|--------------------------------------|
| [templates/RemoteConfigDataSource.kt](templates/RemoteConfigDataSource.kt)         | `:data` `remoteConfig/dataSource/`   |
| [templates/RemoteConfigRepository.kt](templates/RemoteConfigRepository.kt)         | `:domain` `repository/remoteConfig/` |
| [templates/RemoteConfigRepositoryImpl.kt](templates/RemoteConfigRepositoryImpl.kt) | `:data` `remoteConfig/repository/`   |
| [templates/FetchRemoteConfigUseCase.kt](templates/FetchRemoteConfigUseCase.kt)     | `:domain` `usecase/remoteConfig/`    |

Fill `saveValues()` from Step 0 keys (`remoteDataSource.getInt(appOpen)` → `rcAppOpen = …`). Always `saveValues()` even when fetch fails (keep last / default cache). Register live listener → `saveValues()` again.

Logs: `TAG_REMOTE_CONFIG`, `ClassName: functionName: State: details`.

---

## Step 4 — DI (`lazyModule` only)

```kotlin
val dataModule = lazyModule {
    //// DataSources
    single { RemoteConfigDataSource() }
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = lazyModule {
    //// RemoteConfig
    factory { FetchRemoteConfigUseCase(get()) }
}
```

Register in composition root. No `module { }`. Presentation must not depend on `:data`.

---

## Step 5 — EntranceFragment

After Koin is ready (same place as billing init if present):

```kotlin
viewModel.handleIntent(EntranceIntent.FetchRemoteConfig)
```

ViewModel:

```kotlin
is EntranceIntent.FetchRemoteConfig -> fetchRemoteConfigUseCase()
```

No `FetchRemoteConfig` start/success ViewModel logs (repository already logs).

If there is no `EntranceFragment`: **AskQuestion** for the host (prefer `nav_graph` `startDestination`). Do not fetch only from `Application` unless the user says so.

---

## Step 6 — Verify

- [ ] `firebase-config` in catalog + module `implementation`
- [ ] `minimumFetchIntervalInSeconds(0)`
- [ ] `saveValues()` writes every confirmed key to `SharedPrefManager`
- [ ] Features read cache via `SharedPrefRepository` / manager — not RC SDK in UI
- [ ] `FetchRemoteConfigUseCase` in `:domain`; repository interface in `:domain`
- [ ] Entrance (or agreed host) calls the UseCase
- [ ] `InternetManager` check before fetch
- [ ] `lazyModule` only
- [ ] Unit test `FetchRemoteConfigUseCase` with fake repo if `writeTestsWithFeatures: true`

## Do not

- Read Remote Config from Fragments
- `PlatformFirebase` with `Context`
- `remote_config_defaults.xml` unless the user asks
- Rewrite a working legacy RC helper without approval
- Analytics / `EventsProvider` (use `implement-firebase-events`)
- `FirebaseMessagingService` / FCM UI
