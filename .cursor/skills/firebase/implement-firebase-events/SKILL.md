---
name: implement-firebase-events
description: First-time Firebase Analytics for the full app (shared screen_view and ui_click, EventsProvider). Use when adding Analytics events app-wide, EventsProvider, postScreenView, or /implement-firebase-events — not for a few screens (use add-firebase-events) and not for Remote Config.
---

# Implement Firebase Events (full app, first time)

Follow `.cursor/rules/22-platform-firebase.mdc`, `12-naming-conventions.mdc`, `16-logging.mdc`, `08-gradle.mdc`, `00-global.mdc`.  
Shared event rules: [events.md](../events.md).

Obey `.cursor/project-settings.json` when present.

**Requires human approval** before adding `firebase-analytics` if it is not in the catalog.

For extra screens later → `add-firebase-events`.

## Entry

| App state | Action |
|-----------|--------|
| `EventsProvider` + `postScreenView` + most screens already instrumented | Stop. Point user to `add-firebase-events`. |
| Shipped analytics history under other event names | Stop. Do not rename shipped events. |
| Poster exists but still uses `String.postFirebaseEvent()` and nothing has shipped | Continue. Replace that poster with `postScreenView` / `postUiClick` from [events.md](../events.md). |
| No events | Continue. |

---

## Step 0 — Ask before coding

List every user-visible Fragment / Compose Screen / dialog / sheet.

If a screen has no name in [events.md](../events.md), **AskQuestion** for the snake_case `screen_name` before adding a constant. Do not invent one.

Splash, when the app has one: `splash_ft_screen` on first launch after install, `splash_st_screen` on every later launch.

---

## Step 1 — Catalog

Latest stable, `# Firebase` / `// Firebase`:

```toml
firebaseBom = "…"   # latest stable
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
```

```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)   // :core-platform (or :app if single-module)
```

`:app` still needs `google-services` + `google-services.json` before events can reach GA4.

Ensure `Constants.TAG_FIREBASE` exists (`16-logging`).

In the **application** manifest:

```xml
<meta-data
    android:name="google_analytics_automatic_screen_reporting_enabled"
    android:value="false" />
```

---

## Step 2 — EventsProvider + poster

Multi-module greenfield:

1. Copy [templates/firebase/EventsProvider.kt](../../project/setup-new-project/templates/firebase/EventsProvider.kt) → `:core-common`. Keep only screen and element constants for screens that exist, plus the closed event list and parameter constants.
2. Copy [templates/firebase/PlatformFirebase.kt](../../project/setup-new-project/templates/firebase/PlatformFirebase.kt) → `:core-platform` `firebase/PlatformFirebase.kt` when the object is missing (`recordException` + `postScreenView` + `postUiClick` + `getDeviceToken`). No `Context` field. No ads-revenue helper in this skill.

Single-module: same two types next to existing helpers. Ask before inventing a new module.

Add an element constant for each real control, using [events.md](../events.md) (`language_continue_button`, `home_premium_icon`, …).

---

## Step 3 — Wire every screen

**Screen** — xml Fragment `onStart` (once each time that destination starts, including return from another screen or an ad). Do not log the parent again when a dialog that only paused it is dismissed.

```kotlin
override fun onStart() {
    super.onStart()
    PlatformFirebase.postScreenView(
        screenName = EventsProvider.HOME_SCREEN,
        screenClass = HomeFragment::class.java.simpleName,
    )
}
```

**compose:** post `postScreenView` when `*Screen` is shown, once per entry.

Host Activity sets the entry source before the first screen starts. It does not also log `screen_view` for that fragment.

```kotlin
PlatformFirebase.setEntrySource(EventsProvider.ENTRY_ORGANIC)
```

**Tap** — on the click handler: `postUiClick`. If that tap is a semantic event in [events.md](../events.md) (`language_confirm`, `iap_start`, `exit_confirm`, …), log that event as well.

Do **not** auto-log in `ParentFragment`.

---

## Step 4 — Verify

- [ ] One `EventsProvider` (no parallel constant objects)
- [ ] No raw event string literals in UI
- [ ] Event names are only the closed list in [events.md](../events.md)
- [ ] Screen and element values are lowercase snake_case constants
- [ ] Automatic screen reporting is off
- [ ] Poster has no `Context` on `PlatformFirebase`
- [ ] All discovered screens covered (or listed as skipped with reason)

## Do not

- Remote Config (`implement-firebase-remote-config`)
- New modules / migrate folder layout without approval
- Second `EventsProvider`
- One event name per screen or button
- Rename a name that has already shipped
- PII in event names or bundles
