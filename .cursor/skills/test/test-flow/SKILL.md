---
name: test-flow
description: Write coroutine and Flow behavior tests for repositories and UseCases. Use when testing Flow emissions, flowOn, repository streaming, or asking for flow tests. Part of test-* suite.
---

# Flow / Coroutine Tests

Follow `.cursor/rules/11-testing.mdc`, `06-coroutines-flow.mdc`, `18-errors-result.mdc`.

Obey `.cursor/project-settings.json` (`writeTestsWithFeatures`).

## Scope

- Repository / UseCase APIs that return `Flow` or use `withContext`
- Assert emission order, completion, and failure mapping
- Prefer fakes for DataSources — no real network/DB

## Approach

1. Use `runTest` + `StandardTestDispatcher` / `UnconfinedTestDispatcher`
2. Collect with Turbine **only if** already in catalog + approved; otherwise use `first()` / `toList()` with care on finite flows
3. Advance virtual time when testing delays/debounce
4. Verify `CancellationException` is not swallowed incorrectly

## Naming

```
<Unit>_when_<condition>_then_<result>
```

## Example shape

```kotlin
@Test
fun repository_whenDataSourceEmits_thenMapsToDomain() = runTest {
    fakeDataSource.items = listOf(dto)
    val emissions = repository.observeItems().take(1).toList()
    assertEquals(expectedDomain, emissions.first().first())
}
```

## Forbidden

- Collecting unbounded hot flows without timeout in tests
- Real Room/Retrofit in unit/flow tests
- Blocking `runBlocking` outside `runTest` helpers
