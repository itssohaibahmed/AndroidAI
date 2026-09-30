---
description: Entrance screen funnel routing and startup
paths:
  - "**/entrance/**"
  - "**/EntranceFragment*"
  - "**/EntranceScreen*"
  - "**/GetEntranceDestination*"
---

# Entrance screen

Start destination of the app (`nav_graph` / compose `ENTRANCE_ROUTE`). Ads stay on `:gmaAds` extensions — **not** MVI (`21-ads-billing`).

## Flow (prefs)

Use SharedPref / domain UseCase (demo names):

| Pref | Meaning |
|------|---------|
| `isFirstTime` | Cleared when onboarding completes |
| `isLanguageCompleted` | Language funnel done |
| `isOnBoardingCompleted` | Onboarding done |

Typical destination choice (adjust for marketing):

1. Returning user (`!isFirstTime`) → WelcomeBack (or Dashboard)
2. Else if language incomplete → Language
3. Else → OnBoarding

Implement via `GetEntranceDestinationUseCase` (or equivalent) — heavy logic not in the Fragment.

## UI / VM

- Helper: `screenStarted()` → `launchWhenResumed { handleIntent(ScreenStarted) }` when work must wait for resume
- Wait for Koin if resolving deps at cold start (`23-app-startup`)
- MainActivity: Entrance uses `includeTopPadding = false`; back press blocked
- Remote Config fetch starts here (domain/data, not live RC in UI)
- **No Entrance ads:** navigate when Remote Config returns or after **5 seconds** (`setup-new-project`)
- **Entrance ads wired:** ignore the Remote Config result. Then consent (8s) → AdMob init → ad calls (8s) → navigate. Do not navigate from Remote Config (`implement-admob-ads`)

## Forbidden

- Creating missing Language/Onboarding screens only for ads
- Navigating from ViewModel
- Showing Entrance with system-bar padding as if it were a content screen (use MainActivity padding flag)
