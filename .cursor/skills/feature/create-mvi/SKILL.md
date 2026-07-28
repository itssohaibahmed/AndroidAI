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
4. **ViewModel** — `StateFlow` + `SharedFlow`, `CoroutineExceptionHandler`, inject dispatchers
5. **Fragment** — extend `Parent*` / `Base*`; `collectWhenStarted` / `collectWhenCreated`; View Binding only
6. **Mapping** — heavy work in Repo/UseCase; `toUi()` in ViewModel with dispatcher if large lists
7. **Logs** — `Constants.TAG*` format: `ClassName: functionName: State: details`

## Domain + data (if new capability)

- Interface in `:domain`, impl + DataSource in `:data`
- Register `single<Repo> { Impl }` + `factory { UseCase }`
- Presentation must **not** depend on `:data`

## DI

```kotlin
val featurePresentationModule = lazyModule {
    viewModel { FeatureViewModel(get(), get()) }
}
```

Register in app composition root (`KoinModules`).

## Navigation

- Add destination to `nav_*.xml`; Safe Args if needed
- ViewModel emits navigation **Effect**; Fragment calls `NavController`

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
