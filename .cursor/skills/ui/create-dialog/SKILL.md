---
name: create-dialog
description: Create Android dialog XML (dialog_*.xml) from a Figma link or name. XML only — orchestrates figma-to-xml. Use when the user asks for a dialog layout or shares a Figma dialog node.
---

# Create Dialog Layout (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`, `12-naming-conventions.mdc`.

Obey `.cursor/project-settings.json` when present.

## Orchestration

1. If user provided a **Figma URL**, run the **`figma-to-xml`** workflow (including mandatory `figma-design-to-code` before `get_design_context`)
2. Force output type **Dialog** → `dialog_<name>.xml` in `:presentation` `res/layout/`
3. If no Figma — create dialog XML using the same Material / ID / string rules as `figma-to-xml`

## Output

| Type   | Name              |
|--------|-------------------|
| Dialog | `dialog_<name>.xml` |

## Rules

- **Material** widgets (`MaterialTextView`, `MaterialButton`, `MaterialCardView`, `ShapeableImageView`) — never plain `ImageView`
- Clickable icons → `MaterialButton` `style="@style/ButtonStyle.IconButton"` with `app:icon`, `android:padding="4dp"` — **not** clickable `ShapeableImageView`
- Button solid + stroke → `backgroundTint` / `strokeColor` / `strokeWidth` / `cornerRadius` on the button — **no** `bg_shape_*` drawable
- Filled/text `MaterialButton`: `layout_height="wrap_content"` — **no** fixed height + `insetTop`/`insetBottom` `0dp`
- Clickable language/chip selectors → `MaterialButton` + Material background + `app:icon` / `iconGravity="end"` — **not** `MaterialTextView` + `bg_shape_*` / `drawableEnd`, **not** `LinearLayout` + Text + ImageView
- View Binding only (no Data Binding attributes)
- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed — unless `project-settings.json` says otherwise
- Shallow nesting — ConstraintLayout primary; avoid deep LinearLayout trees
- View IDs: Hungarian prefix + camelCase (`mtvTitleDialog`, `mbConfirmDialog`, `clRootDialog`)
- Theme attrs: `?attr/colorSurface`, `?attr/colorOnBackground`
- Inline `dp` / `sp` only — **no `dimens.xml`**; sizes in **multiples of 4**
- No hardcoded user-facing strings — `@string/` from `:core-ui`
- Images: `android:contentDescription="@string/cd_…"`
- Closing tags: blank line between nested container closes; **no** extra blank line after the root closing tag
- Prefer small confirmations / alerts as dialogs; medium filters → `create-bottom-sheet`
- Prefer `ParentDialog` / project base dialog when wiring later

## Do not create

- Kotlin DialogFragment/ViewModel (use `create-mvi` or wire to existing host)
- Strings in presentation module — add to shared `strings.xml` (incl. `cd_*`)
- `dimens.xml`
- `findViewById` references
- Plain `ImageView`
- Clickable `ShapeableImageView` used as a button

## After layout

Tell user to wire via `create-mvi` or existing Fragment if Kotlin not requested.
