---
description: Optional ads and in-app billing / premium patterns
paths:
  - "**/gmaAds/**"
  - "**/admob*/**"
  - "**/billing/**"
  - "**/premium/**"
  - "**/*.gradle.kts"
---

Apply only when the project includes ads and/or IAP. Do not add these modules without approval.

## Ads module (e.g. `:gmaAds`)

- Keep ads in a dedicated module with internal data/domain/presentation (or managers) packages
- Presentation may depend on the ads module; feature Fragments use existing managers / ad ViewModels — never raw AdMob SDK calls scattered in every screen
- Gate load/show on: **premium purchase flag** + **Remote Config** flags when the project uses them
- Use named DI qualifiers for parallel placements (`named("banner_home")`, interstitial keys enum)

## Ad unit IDs

```kotlin
// build.gradle.kts of ads module
debug { resValue("string", "admob_banner_home_id", "ca-app-pub-3940256099942544/…") } // Google sample
release { resValue("string", "admob_banner_home_id", "ca-app-pub-…") } // production
```

- Debug = Google sample IDs only
- Release = production IDs
- Never commit the wrong ID into the wrong build type
- App ID via manifest/`resValue` — not hardcoded in Kotlin

## Billing / premium

- Billing manager lives behind a domain `BillingRepository` (impl in `:data`)
- Persist entitlement (e.g. `isAppPurchased`) in SharedPreferences / DataStore via repository
- Premium screens follow normal MVI feature packages (`premium/`)
- Product IDs: constants in data/domain — not duplicated in UI
- After purchase success: update entitlement, then let ads/UI react
- Full subs + in-app playbook: [reference/premium-billing.md](reference/premium-billing.md)
- Skills: `premium/implement-in-app-billing`, `premium/add-subscription-packages`, `premium/add-inapp-packages`

## Forbidden

- Loading ads when user is premium (unless product says otherwise)
- Production ad IDs in debug builds
- Calling BillingClient / MobileAds directly from random Fragments when managers/repos exist
- Hardcoding secrets or keystore passwords next to billing setup