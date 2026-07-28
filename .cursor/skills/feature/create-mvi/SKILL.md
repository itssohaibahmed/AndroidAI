---
name: create-mvi
description: Scaffold a full Android MVI feature (Intent/State/Effect/ViewModel/Fragment/DI/tests). Use when adding a new screen with business logic, ViewModel, or MVI packages under presentation.
---

# Create MVI Feature

Follow `.cursor/rules/` (especially `01-ai-agent`, `03`, `04`, `06`, `07`, `17`, `18`, `19`).

## Preconditions

- Confirm feature name (camelCase folder, e.g. `userProfile`)
- Confirm if new domain capability needed (UseCase + Repository) or UI-only on existing use cases
- Read existing similar feature for patterns (base Fragment, DI module naming, nav)

## Package layout (`:presentation`)

```
presentation/<featureName>/
  di/<Feature>PresentationModule.kt
  intent/<Feature>Intent.kt
  state/<Feature>State.kt
  effect/<Feature>Effect.kt
  viewModel/<Feature>ViewModel.kt
  ui/<Feature>Fragment.kt
  adapter/     # if list
  mapper/      # if domain → UI mapping
```

## Kotlin checklist

1. **Intent** — sealed class; UI dispatches only via `handleIntent`
2. **State** — data class, defaults, computed `show*` flags
3. **Effect** — navigation, permissions, `@StringRes` errors
4. **ViewModel** — follow `04-mvi-presentation` structure:
   - `handleIntent` = single `viewModelScope.launch(exceptionHandler) { when … }`
   - handlers = `private suspend fun onX()`
   - `exceptionHandler` → `handleError`; **`handleError` last** in the class
   - sparse logs (repo primary; ViewModel failures/`Log.w` guards only)
5. **Fragment** — extend `Parent*` / `Base*`; `collectWhenStarted` / `collectWhenCreated`; View Binding only
6. **Mapping** — heavy work in Repo/UseCase; `toUi()` in ViewModel with dispatcher if large lists
7. **Logs** — `Constants.TAG*` format; prefer Repository; ViewModel not every method

## Domain + data (if new capability)

```
domain/
  repository/<area>/<Name>Repository.kt     # interface ONLY
  usecase/<area>/<Name>UseCase.kt           # UseCase ONLY
  di/UseCaseModule.kt                       # lazyModule { factory { … } }

data/
  <area>/dataSource/…                       # DataSource / manager
  <area>/repository/<Name>RepositoryImpl.kt # impl ONLY
  di/DataModule.kt                          # lazyModule { //// DataSources … //// Repositories … }
```

- **Never** create UseCase or repository interface under `:data`
- Register `factory { UseCase }` in **domain** `useCaseModule`
- Register DataSources then Repositories in **data** `dataModule` (two sections)
- Presentation must **not** depend on `:data`

## DI

```kotlin
val featurePresentationModule = lazyModule {

    //// ViewModels
    viewModel { FeatureViewModel(get(), get()) }
}

val useCaseModule = lazyModule {

    //// Feature
    factory { GetFeatureUseCase(get()) }
}

val dataModule = lazyModule {

    //// DataSources
    single { FeatureDataSource() }

    //// Repositories
    single<FeatureRepository> { FeatureRepositoryImpl(get(), get()) }
}
```

Register **all** of the above in app composition root (`KoinModules`). Always `lazyModule` — never `module { }`. Use `//// Section` headers for separation of concerns (`07-dependency-injection`).

## Navigation

- Add destination to `nav_*.xml`; Safe Args if needed
- ViewModel emits navigation **Effect**; Fragment calls `NavController`
- Every `<action>` must use reference slide anims (`17-navigation`):

```xml
app:enterAnim="@anim/slide_in_right"
app:exitAnim="@anim/slide_out_left"
app:popEnterAnim="@anim/slide_in_left"
app:popExitAnim="@anim/slide_out_right"
```

## Strings

- All copy in single `:core-ui` `strings.xml` (+ locale folders)

## Tests

- UseCase unit test (fake repo)
- ViewModel test: Intents → State + Effects (`runTest`)

## Verify

- [ ] No `:presentation` → `:data` dependency
- [ ] Portrait + landscape layouts
- [ ] ProGuard keeps `state`/`intent`/`effect`/`model` packages
- [ ] No business logic in Fragment beyond render + intent dispatch
