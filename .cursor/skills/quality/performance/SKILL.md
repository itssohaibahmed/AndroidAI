---
name: performance-review
description: Review Android code for ANR risk, large dataset handling, dispatchers, and RecyclerView performance. Use when optimizing lists, fixing jank, ANR, or slow screens.
---

# Performance Review

Follow `.cursor/rules/06-coroutines-flow.mdc`, `03`, `04`.

## Assume thousands of items / large files always

### Critical checks
- [ ] No `runBlocking`, sync disk, or network on Main
- [ ] Large `map`/`filter`/`sort` on `Default` or `IO`
- [ ] Repository/UseCase does heavy mapping — not Fragment/Adapter
- [ ] `submitList` only after off-Main list prep
- [ ] No `notifyDataSetChanged` on large RecyclerViews

### RecyclerView
- [ ] `ListAdapter` + `DiffUtil`
- [ ] `app:layoutManager` / `android:orientation` / `app:spanCount` in XML — Kotlin only if dynamic
- [ ] Stable IDs when beneficial
- [ ] Avoid nested RecyclerViews with heavy child rebind
- [ ] Glide/image loads cancelled on rebind

### State / memory
- [ ] State does not hold full raw megabyte datasets unnecessarily
- [ ] Pagination / windowed load for open-ended data
- [ ] Prefer `Flow` streaming over load-all-in-memory

### Layout
- [ ] Shallow hierarchy (`09`) — overdraw / measure cost
- [ ] No unnecessary `layout-land` duplication if one responsive layout suffices

### Coroutines
- [ ] `flowOn` before collection for upstream IO
- [ ] No `GlobalScope`

## Report format

| Issue | Location | Severity | Fix |
|-------|----------|----------|-----|
| Main-thread map of 5000 items | `XFragment` | Critical | Move to ViewModel + Default |

Prioritize **Critical** = ANR or OOM risk on realistic data sizes.
