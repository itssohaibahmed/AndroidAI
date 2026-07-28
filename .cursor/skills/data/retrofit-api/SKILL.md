---
name: retrofit-api
description: Add Retrofit networking with Clean Architecture. Use when adding REST API, remote DataSource, or HTTP client. Requires explicit library approval via version catalog.
---

# Retrofit API

Follow `.cursor/rules/08`, `13`, `06`, `18`, `22`. **Add Retrofit/OkHttp/Moshi only with approval** — catalog first.

Prefer existing `InternetManager` (`:core-platform`) for connectivity checks before calls.

## Layers

| Layer | Contents |
|-------|----------|
| Data | `ApiService` (Retrofit), DTOs, `RemoteDataSource`, `*RepositoryImpl` |
| Domain | Domain models + repository interface — no Retrofit types |
| Core-platform | OkHttp client, interceptors, base URL config |

## Setup

1. `libs.versions.toml`: retrofit, okhttp, converter (moshi/gson)
2. `:core-platform` or `:data`: OkHttp + Retrofit `single` in DI
3. Base URL from build config / `local.properties` — not hardcoded secrets

```kotlin
interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
}
```

## Error handling

- Map HTTP/IO errors at data boundary to typed `DataError` / `Outcome` (`18-errors-result`)
- Never leak status codes or raw JSON bodies into UI State
- Log: `Constants.TAG` — `UserRepositoryImpl: getUser: Failed: ${e.message}`

## Dispatchers

- All network on `IO` in **Repository** (`withContext`); DataSource stays thin
- Repository returns domain models or typed failures

## DI

```kotlin
single { provideRetrofit(get()) }
single { get<Retrofit>().create(UserApi::class.java) }
single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
```

## Forbidden

- Retrofit interfaces in domain/presentation
- Blocking calls on Main
- Logging auth tokens or full PII
- New networking stack if project already has one — extend existing client
