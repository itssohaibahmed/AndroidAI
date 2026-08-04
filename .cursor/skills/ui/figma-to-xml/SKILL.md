---
name: figma-to-xml
description: Create Android XML layouts and drawables from a Figma URL or freeform screen request (fragment_/activity_/item_/layout_). XML only — no Kotlin/MVI. Use when the user shares a Figma link, asks for Figma-to-XML, or wants a screen layout without ViewModel scaffolding. Loads Figma design-to-code skill, then adapts to project rules.
---

# Figma / Screen → Android XML (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`, `12-naming-conventions.mdc`, `24-figma-assets.mdc`.

Obey `.cursor/project-settings.json` when present (`orientation`, `themeModes`).

**Never** create Kotlin Fragment/ViewModel/DI here — point user to `create-mvi` / `create-clean-architecture` afterward.

## Workflow

### A — From Figma link

1. Parse Figma URL → `fileKey` + `nodeId` (`-` → `:` in node id)
2. **Mandatory:** load Cursor Figma plugin skill **`figma-design-to-code`** before calling `get_design_context`
3. Call `get_design_context` (+ screenshot if needed)
4. Adapt to **this** project: Material widgets, Hungarian IDs, single `:core-ui` strings, no Compose
5. Download assets with Figma MCP `download_assets`
6. Build layout XML + drawables + strings only

### B — Freeform (no Figma)

Same output rules as below — create the requested `fragment_*` / `activity_*` / `item_*` / `layout_*` XML without Figma MCP.

## Output

One (or more) layout file(s) in `:presentation` `res/layout/` (and `layout-land/` when `orientation` is `both` or `landscape` requires it):

| Type            | Name                                                       |
|-----------------|------------------------------------------------------------|
| Fragment screen | `fragment_<feature>.xml` or `fragment_<feature>_<sub>.xml` |
| Activity        | `activity_<name>.xml`                                      |
| Reusable block  | `layout_<name>.xml`                                        |
| List row        | `item_<name>.xml`                                          |

Dialog / bottom sheet: use `create-dialog` / `create-bottom-sheet` (they orchestrate this skill with `dialog_*` / `bottom_sheet_*` names).

## Assets — prefer SVG

Per `24-figma-assets`:

- Prefer **SVG** / `svgAssets` for icons, logos, simple vectors → `ic_svg_*` / `img_svg_*` / `bg_svg_*`
- Use `defaultFormat: "svg"` for vector nodes unless user asked for raster
- Fall back to PNG/WebP when photo, complex art, or SVG is broken/huge
- Place drawables in `:core-ui` (not app `values`)

## Rules

- **Material** widgets (`MaterialTextView`, `MaterialButton`, `MaterialCardView`, `ShapeableImageView`) — never plain `ImageView`
- Clickable icons → `MaterialButton` `style="@style/ButtonStyle.IconButton"` with `app:icon`, `android:padding="4dp"` — **not** clickable `ShapeableImageView`
- Button solid + stroke → `backgroundTint` / `strokeColor` / `strokeWidth` / `cornerRadius` on the button — **no** `bg_shape_*` drawable
- Filled/text `MaterialButton`: `layout_height="wrap_content"` — **no** fixed height + `insetTop`/`insetBottom` `0dp`
- Clickable language/chip selectors → `MaterialButton` + Material background + `app:icon` / `iconGravity="end"` — **not** `MaterialTextView` + `bg_shape_*` / `drawableEnd`, **not** `LinearLayout` + Text + ImageView
- View Binding only (no Data Binding attributes)
- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed — **unless** `project-settings.json` `orientation` is `portrait` or `landscape` only
- Theme modes: add `values-night` colors/themes when `themeModes` is `night` or `both`
- Shallow nesting — ConstraintLayout primary; avoid deep LinearLayout trees
- View IDs: Hungarian prefix + camelCase (`mtvTitleHome`, `sivLogoHome`, `mbConfirmLanguage`, `clRootHome`)
- Theme attrs: `?attr/colorSurface`, `?attr/colorOnBackground`
- Inline `dp` / `sp` only — **no `dimens.xml`**; sizes in **multiples of 4** (`8dp`, `16dp`, `16sp`)
- No hardcoded user-facing strings — `@string/` from `:core-ui`
- Images: `android:contentDescription="@string/cd_…"` (add `cd_*` under Content Descriptions section)
- Programmatic loads → `siv.loadImage(...)` (Glide / `ImageViewExtensions`) when documenting bind notes
- RecyclerView: `app:layoutManager` + `android:orientation` / `app:spanCount` in XML — not in Kotlin unless dynamic; rows = `item_<name>.xml`
- Closing tags: blank line between nested container closes (`</ConstraintLayout>` → blank → `</MaterialCardView>` → blank → root); **no** extra blank line after the root closing tag
- **Button fill + stroke from Figma:** apply on `MaterialButton` with `app:backgroundTint`, `app:strokeColor`, `app:strokeWidth`, `app:cornerRadius` — **do not** export/create `bg_shape_*` oval/rect (solid+stroke only), and **do not** use `android:background` + `backgroundTint="@null"` + inset hacks
    - Circle icon button: `cornerRadius` ≈ half of width/height
    - Colors → `:core-ui` `colors.xml`; skip the shape XML file entirely when Material attrs cover it
    - Only create `bg_shape_*` for non-button surfaces, gradients, or selectors Material cannot express

## Do not create

- Kotlin Fragment/ViewModel (use `create-mvi` skill)
- Domain / data / repository layers (use `create-clean-architecture`)
- Strings in presentation module — add to shared `strings.xml` (incl. `cd_*`)
- `dimens.xml`
- `findViewById` references
- Plain `ImageView`
- Clickable `ShapeableImageView` used as a button

## After layout

- Call out any asset that fell back from SVG → raster and why
- Tell user to wire via `create-mvi` (presentation) and `create-clean-architecture` if new domain/data is needed
