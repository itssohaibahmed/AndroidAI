---
name: figma-to-xml
description: Implement a Figma screen/link as Android XML layouts and drawables. Use when the user shares a Figma URL, asks for Figma-to-XML, or wants design-to-code with View Binding layouts. Prefers SVG assets from Figma MCP when safe.
---

# Figma → Android XML

Follow `.cursor/rules/09-resources-xml.mdc`, `24-figma-assets.mdc`, and Figma design-to-code skill before `get_design_context`.

## Workflow

1. Parse Figma URL → `fileKey` + `nodeId` (`-` → `:` in node id)
2. Load Figma design-to-code guidance; call `get_design_context` (+ screenshot if needed)
3. Adapt to **this** project: Material widgets, Hungarian IDs, single `:core-ui` strings, no Compose
4. Download assets with Figma MCP `download_assets`
5. Build `fragment_*` / `item_*` XML only (or full MVI if user asks `create-mvi`)

## Assets — prefer SVG

Per `24-figma-assets`:

- Prefer **SVG** / `svgAssets` for icons, logos, simple vectors → `ic_svg_*` / `img_svg_*` / `bg_svg_*`
- Use `defaultFormat: "svg"` for vector nodes unless user asked for raster
- Fall back to PNG/WebP when photo, complex art, or SVG is broken/huge
- Place drawables in `:core-ui` (not app `values`)

## Layout rules (quick)

- `ShapeableImageView` (`siv`) for display-only images
- Programmatic loads → `siv.loadImage(...)` (Glide / `ImageViewExtensions`)
- Clickable language/chip selectors → `MaterialButton` + Material style + `app:icon` / `iconGravity="end"` (no `bg_shape_*` chip bg, no MTV + drawableEnd)
- Clickable icons → `MaterialButton` + `style="@style/ButtonStyle.IconButton"` (`mb` + `app:icon`, `android:padding="4dp"`) — not clickable `siv`
- **Button fill + stroke from Figma:** apply on `MaterialButton` with `app:backgroundTint`, `app:strokeColor`, `app:strokeWidth`, `app:cornerRadius` — **do not** export/create `bg_shape_*` oval/rect (solid+stroke only), and **do not** use `android:background` + `backgroundTint="@null"` + inset hacks
  - Circle icon button: `cornerRadius` ≈ half of width/height
  - Colors → `:core-ui` `colors.xml`; skip the shape XML file entirely when Material attrs cover it
  - Only create `bg_shape_*` for non-button surfaces, gradients, or selectors Material cannot express
- Material text/buttons/cards
- RecyclerView: `app:layoutManager` + orientation / `spanCount` in XML (not Kotlin unless dynamic)
- `cd_*` content descriptions
- No `dimens.xml` — spacing/text sizes multiples of 4
- Portrait + landscape
- Strings in `:core-ui` `strings.xml` (app → general → content descriptions → screen)

## Output

- Layout XML + drawables + strings
- Call out any asset that fell back from SVG → raster and why
