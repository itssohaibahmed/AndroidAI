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

- `ShapeableImageView` only (`siv`)
- Material text/buttons/cards
- `cd_*` content descriptions
- No `dimens.xml` — spacing/text sizes multiples of 4
- Portrait + landscape
- Strings in `:core-ui` `strings.xml` (app → general → content descriptions → screen)

## Output

- Layout XML + drawables + strings
- Call out any asset that fell back from SVG → raster and why
