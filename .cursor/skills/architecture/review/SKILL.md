---
name: architecture-review
description: Review Android changes against Clean Architecture, MVI, module boundaries, and project rules. Use when reviewing PRs, refactors, or asking if code follows architecture standards.
---

# Architecture Review

Read `.cursor/rules/` and apply systematically. Output a structured report.

## Checklist

### Module boundaries
- [ ] `presentation` does not import `:data`
- [ ] `domain` has no Android UI / presentation imports
- [ ] No circular module dependencies
- [ ] New `lazyModule` registered in composition root

### MVI
- [ ] Intent / State / Effect / ViewModel pattern
- [ ] Navigation via Effects — not NavController in ViewModel
- [ ] No mutable state exposed publicly
- [ ] Fragments render + dispatch intents only

### Mapping
- [ ] Heavy mapping in Repo / UseCase — not Fragment/Adapter
- [ ] `toUi()` in ViewModel only when needed, with dispatcher for large lists
- [ ] Adapters bind `*UiItem` only

### Threading / ANR (`06`, `quality/performance`)
- [ ] No disk/network/heavy map on Main
- [ ] Injected dispatchers where project uses them
- [ ] Large lists: ListAdapter + DiffUtil

### UI (`09`, `19`)
- [ ] View Binding only — no findViewById / Data Binding
- [ ] Material widgets; portrait + landscape
- [ ] Strings in single `:core-ui` file

### Security / logging (`14`, `16`)
- [ ] No secrets in code/commits
- [ ] `Constants.TAG*` log format
- [ ] No PII in logs

### Errors (`18`)
- [ ] Typed failures — not raw exceptions in State
- [ ] CancellationException handled correctly

## Report format

```markdown
## Summary
One-line verdict: Pass / Pass with notes / Fail

## Critical (must fix)
- ...

## Warnings (should fix)
- ...

## Suggestions (optional)
- ...

## Rules referenced
- rule files that applied
```

Severity: boundary violations and Main-thread heavy work = **Critical**.