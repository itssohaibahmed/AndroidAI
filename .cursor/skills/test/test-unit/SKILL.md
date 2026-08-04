---
name: test-unit
description: Write Android unit tests for UseCases and ViewModels. Use when adding unit tests, MVI test coverage, or fake repository tests. Part of test-* suite; use test-complete to run everything.
---

# Unit Tests

Follow `.cursor/rules/11-testing.mdc`, `18-errors-result.mdc`.

Obey `.cursor/project-settings.json`: if `writeTestsWithFeatures` is `false`, only write tests when the user explicitly asks.

## Priority

1. **UseCases** — fake domain repositories, JVM tests
2. **ViewModels** — Intents → State + Effects
3. **Mappers** — pure Kotlin when non-trivial

Skip trivial getter tests and template-only assertions.

## Naming

```
<Unit>_when_<condition>_then_<result>
```

## UseCase test

```kotlin
class GetUserUseCaseTest {
    private val repository = FakeUserRepository()
    private val useCase = GetUserUseCase(repository)

    @Test
    fun getUser_whenRepositoryReturnsUser_thenReturnsUser() = runTest {
        repository.user = User(id = "1", name = "Test")
        val result = useCase("1")
        assertEquals("Test", result?.name)
    }
}
```

- Fake implements domain interface in `src/test`
- `runTest` for suspend APIs

## ViewModel test

- Drive via `handleIntent`
- Assert `state.value` after emissions (Turbine only if already in catalog + approved)
- Collect effects in test scope for one-shot events
- Inject test dispatchers (`StandardTestDispatcher` / `UnconfinedTestDispatcher`)
- No real Android framework — mockK only if already approved in project

```kotlin
@Test
fun viewModel_whenLoadFails_thenEmitsShowError() = runTest {
    fakeRepo.shouldFail = true
    viewModel.handleIntent(FeatureIntent.Load)
    advanceUntilIdle()
    // assert state error flag or effect
}
```

## Location

Mirror package under `src/test/java`.

## Forbidden

- Instrumentation tests for pure business logic
- Tests that only assert constants
- Real network/DB in unit tests
