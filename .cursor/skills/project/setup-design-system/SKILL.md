---
name: setup-design-system
description: Import a Figma design system into :core-ui (colors, type, ButtonStyle/TextStyle, Material theme, day/night). Theme-first — windowBackground, not layout colorSurface. Use when the user shares a Figma design-system URL, invokes /setup-design-system, or setup-new-project option (a). Do not use for a single screen layout (figma-to-xml) or to bootstrap modules (setup-new-project).
---

# Setup Design System

Follow `.cursor/rules/` — especially `09-resources-xml` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md), `12-naming-conventions`, `24-figma-assets`, `00-global`.

Obey `.cursor/project-settings.json` when present (`themeModes`, `orientation`, `applicationId`, optional `figmaDesignSystemUrl`).

**This skill does not bootstrap the app.** Require `:core-ui`. New apps: `setup-new-project` first.

Extraction file map: [reference.md](reference.md).

## Preconditions

1. **Figma URL** — `figma.com/design/...` in the prompt, or `figmaDesignSystemUrl` in `project-settings.json`. If neither exists, **ask**. Do not guess a file.
2. Persist the URL to `figmaDesignSystemUrl` in `.cursor/project-settings.json` (and `.claude/project-settings.json` if that file exists).
3. Confirm `:core-ui` is on the Gradle graph. If missing, stop and tell the user to run `setup-new-project`.
4. Read `themeModes`: `day` → `values/` only; `night` / `both` → also `values-night/`.

## Steps

1. Parse URL → `fileKey` + `nodeId` (`-` → `:`). Load **figma-design-to-code** before `get_design_context`; load **figma-use** before `use_figma`.
2. Extract tokens with **targeted** reads only (Foundation / Tokens pages, local text styles, semantic light/dark rows). No whole-file variable dumps.
3. Write `:core-ui` resources per [reference.md](reference.md): primitive + semantic colors, `md_theme_*`, fonts, `TextStyle.*`, `ButtonStyle.*`, `ShapeAppearance.App.*`, theme items including **`android:windowBackground`**.
4. **Theme-first:** default screen color lives on the theme. Do **not** set `android:background="?attr/colorSurface"` (or equivalent) on default layout roots. Layout `android:background` only when that region is a **different** surface.
5. Do not invent screens, `dimens.xml`, or port the full icon/component library (screens stay `figma-to-xml`).
6. `assembleDebug`. Report what landed (palette, type family, button styles) and that layouts inherit `windowBackground`.

## Do not

- Scaffold `:app` / `:presentation` / Entrance (that is `setup-new-project`)
- Implement a product screen from this file (`figma-to-xml`)
- Paint default roots with `colorSurface`
- Add always-on rules; XML law stays in `09` + `reference/resources-xml.md`
