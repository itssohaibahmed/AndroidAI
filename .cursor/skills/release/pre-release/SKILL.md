---
name: pre-release
description: Pre-release checklist for Android apps before Play Store upload. Use before release, version bump, or asking if the app is ready to ship.
---

# Pre-Release Checklist

Cross-check `.cursor/rules/` and run manual verification.

## Build
- [ ] Release build succeeds (`assembleRelease` / bundle)
- [ ] `versionCode` / `versionName` updated
- [ ] Release: minify + shrink enabled on `:app`
- [ ] ProGuard rules cover entities, MVI packages, ads entities if applicable
- [ ] No debug `applicationIdSuffix` on release
- [ ] Signing via secure config — not committed passwords

## Configuration
- [ ] Production API keys / ad IDs in release build type only
- [ ] Remote Config / Firebase prod project (if used)
- [ ] `targetSdk` meets Play requirements

## Quality gates
- [ ] Run `review/review-architecture` skill on release diff
- [ ] Run `review/review-security` skill
- [ ] Run `review/review-performance` on list-heavy screens
- [ ] Or run `review/review-complete` for all three
- [ ] Critical flows tested on minSdk device/emulator (`test/test-complete` when useful)

## UX
- [ ] Portrait + landscape on main flows
- [ ] RTL if `supportsRtl=true`
- [ ] No hardcoded UI strings

## Store assets (manual)
- [ ] Screenshots, description, privacy policy URL
- [ ] Data safety form matches actual data collection

## Output

Provide checklist with ✅/❌/⚠️ per item and blockers list before recommending upload.
