---
name: test-unit
description: Write Android JVM unit tests for UseCases, ViewModels, mappers, and Flow behavior with fakes. Use when adding unit tests, MVI coverage, Flow/runTest with fakes, or fake repository tests. Part of test-* suite; use test-integration for real Room/network layers, test-e2e for device UI, test-complete to run everything.
---

# Unit Tests

## Start banner

First user-visible sentence when this skill runs (verbatim):

> We are going to write/update JVM unit tests (UseCases, ViewModels, Flow with fakes) — no emulator or device required.

Follow `.cursor/rules/11-testing.mdc`, `06-coroutines-flow.mdc`, `18-errors-result.mdc`.

Obey `.cursor/project-settings.json`: if `writeTestsWithFeatures` is `false`, only write tests when the user explicitly asks.

## Priority

1. **UseCases** — fake domain repositories, JVM tests
2. **ViewModels** — Intents → State + Effects
3. **Mappers** — pure Kotlin when non-trivial
4. **Flow / coroutine behavior** — UseCase or repository APIs that return `Flow` / use `withContext`, still with **fakes** (no real network/DB)

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

## Flow / coroutine tests (with fakes)

- Assert emission order, completion, and failure mapping
- Prefer fakes for DataSources — no real network/DB
- Use `runTest` + `StandardTestDispatcher` / `UnconfinedTestDispatcher`
- Collect with Turbine **only if** already in catalog + approved; otherwise `first()` / `toList()` on finite flows
- Advance virtual time for delays/debounce
- Do not swallow `CancellationException` incorrectly
- Do not collect unbounded hot flows without a timeout

```kotlin
@Test
fun repository_whenDataSourceEmits_thenMapsToDomain() = runTest {
    fakeDataSource.items = listOf(dto)
    val emissions = repository.observeItems().take(1).toList()
    assertEquals(expectedDomain, emissions.first().first())
}
```

## Location

Mirror package under `src/test/java`.

## Forbidden

- Instrumentation / Espresso for pure business logic → use `test-e2e` only for user-visible device flows
- Real Room / Retrofit / network / DB → use `test-integration`
- Tests that only assert constants
- Blocking `runBlocking` outside `runTest` helpers
