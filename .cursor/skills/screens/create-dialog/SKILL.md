---
name: create-screen
description: Create Android screen XML layout only (fragment_/activity_). Use when user asks for a new screen layout, fragment layout XML, or UI XML without Kotlin/MVI scaffolding.
---

# Create Screen Layout (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`, `12-naming-conventions.mdc`.

## Output

One layout file in `:presentation` `res/layout/`:

| Type | Name |
|------|------|
| Fragment screen | `fragment_<feature>.xml` or `fragment_<feature>_<sub>.xml` |
| Activity | `activity_<name>.xml` |
| Reusable block | `layout_<name>.xml` |

## Rules

- **Material** widgets (`MaterialTextView`, `MaterialButton`, `MaterialCardView`) — not plain TextView/Button
- View Binding only (no Data Binding attributes)
- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed
- Shallow nesting — ConstraintLayout primary; avoid deep LinearLayout trees
- View IDs: Hungarian prefix + camelCase (`mtvTitleHome`, `mbContinueLogin`, `clRootHome`)
- Theme attrs: `?attr/colorSurface`, `?attr/colorOnBackground`
- `dp` layout, `sp` text
- No hardcoded user-facing strings — `@string/...` from `:core-ui`
- RecyclerView rows: separate `item_<name>.xml`

## Do not create

- Kotlin Fragment/ViewModel (use `create-mvi` skill)
- Strings in presentation module — add to shared `strings.xml`
- `findViewById` references

## After layout

Tell user to wire via `create-mvi` or existing Fragment if Kotlin not requested.
