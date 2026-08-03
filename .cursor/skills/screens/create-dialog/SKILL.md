---
name: create-dialog
description: Create Android screen XML layout only (fragment_/activity_/dialog_). Use when user asks for a new screen layout, fragment layout XML, dialog layout, or UI XML without Kotlin/MVI scaffolding.
---

# Create Screen Layout (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`, `12-naming-conventions.mdc`.

## Output

One layout file in `:presentation` `res/layout/`:

| Type            | Name                                                       |
|-----------------|------------------------------------------------------------|
| Fragment screen | `fragment_<feature>.xml` or `fragment_<feature>_<sub>.xml` |
| Activity        | `activity_<name>.xml`                                      |
| Dialog          | `dialog_<name>.xml`                                        |
| Reusable block  | `layout_<name>.xml`                                        |

## Rules

- **Material** widgets (`MaterialTextView`, `MaterialButton`, `MaterialCardView`, `ShapeableImageView`) — never plain `ImageView`
- Clickable icons → `MaterialButton` `style="@style/ButtonStyle.IconButton"` with `app:icon`, `android:padding="4dp"` — **not** clickable `ShapeableImageView`
- Text + static trailing/leading icon (chips/selectors) → **one** `MaterialTextView` with `app:drawableEndCompat` / `app:drawableStartCompat` — **not** `LinearLayout` + Text + ImageView
- View Binding only (no Data Binding attributes)
- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed
- Shallow nesting — ConstraintLayout primary; avoid deep LinearLayout trees
- View IDs: Hungarian prefix + camelCase (`mtvTitleHome`, `sivLogoHome`, `mbConfirmLanguage`, `clRootHome`)
- Theme attrs: `?attr/colorSurface`, `?attr/colorOnBackground`
- Inline `dp` / `sp` only — **no `dimens.xml`**; sizes in **multiples of 4** (`8dp`, `16dp`, `16sp`)
- No hardcoded user-facing strings — `@string/` from `:core-ui`
- Images: `android:contentDescription="@string/cd_…"` (add `cd_*` under Content Descriptions section)
- RecyclerView: `app:layoutManager` + `android:orientation` / `app:spanCount` in XML — not in Kotlin unless dynamic; rows = `item_<name>.xml`

## Do not create

- Kotlin Fragment/ViewModel (use `create-mvi` skill)
- Strings in presentation module — add to shared `strings.xml` (incl. `cd_*`)
- `dimens.xml`
- `findViewById` references
- Plain `ImageView`
- Clickable `ShapeableImageView` used as a button

## After layout

Tell user to wire via `create-mvi` or existing Fragment if Kotlin not requested.