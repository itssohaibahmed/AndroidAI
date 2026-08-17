---
description: MVI pattern — Intent, State, Effect, ViewModel
paths:
  - "**/presentation/**/*.kt"
  - "**/viewModel/**/*.kt"
  - "**/intent/**/*.kt"
  - "**/state/**/*.kt"
  - "**/effect/**/*.kt"
---

# MVI presentation (invariants)

**Full detail (full ViewModel template, DiffUtil, mapping):** [reference/mvi-presentation.md](reference/mvi-presentation.md)

## Must follow

- **Intent** — sealed; UI dispatches only via `viewModel.handleIntent(...)`
- **State** — data class with defaults + derived flags; update via `_state.update { it.copy(...) }`
- **Effect** — one-shot (nav, toast, permission, dialog); prefer `@StringRes`
- ViewModel: single `handleIntent` launch + `exceptionHandler`; handlers are `suspend onX()`; **`handleError` last**; no nested `launch` per intent
- Logging: prefer Repository; ViewModel sparse; always log failures in `handleError`
- Fragment: collect state/effects via `viewLifecycleOwner` helpers; `navigateTo` / `popFrom`; render only; adapters bind `*UiItem`
- Member order per `19-base-ui`: `onViewCreated` (inline clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect`
- Heavy mapping in Repo/UseCase; `toUi()` in VM with dispatcher if heavy — never in Fragment/Adapter
- Large lists: `ListAdapter` + DiffUtil; DiffUtil one-liners; portrait + landscape

Read [reference/mvi-presentation.md](reference/mvi-presentation.md) when scaffolding or changing MVI.
