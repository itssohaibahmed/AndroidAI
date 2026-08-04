---
name: review-complete
description: Run full project/PR review — architecture, performance, and security — into one Pass/Fail report. Use when asking for complete review, review-*, or pre-merge quality gate.
---

# Complete Review

Run these skills in order on the same change set / project:

1. **`review-architecture`**
2. **`review-performance`**
3. **`review-security`**

Optionally note `release/pre-release` items if the user asked about shipping readiness (do not replace a full pre-release pass).

Obey `.cursor/project-settings.json` when present.

## Aggregated report

```markdown
## Overall verdict
Pass / Pass with notes / Fail

## Architecture
(summary + Critical / Warnings from review-architecture)

## Performance
(summary + Critical / Warnings from review-performance)

## Security
(summary + Critical / Warnings from review-security)

## Must-fix before merge
- …

## Optional follow-ups
- …
```

**Fail** if any skill reports a **Critical** finding (module boundary break, Main-thread ANR risk, leaked secret, unsafe exported component).
