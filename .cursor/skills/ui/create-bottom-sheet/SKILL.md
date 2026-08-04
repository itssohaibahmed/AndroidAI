---
name: create-bottom-sheet
description: Create Android bottom sheet XML (bottom_sheet_*.xml) from a Figma link or name. XML only — orchestrates figma-to-xml. Use when the user asks for bottom sheet layout without Kotlin BottomSheetDialogFragment wiring.
---

# Create Bottom Sheet Layout (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`.

Obey `.cursor/project-settings.json` when present.

## Orchestration

1. If user provided a **Figma URL**, run the **`figma-to-xml`** workflow (including mandatory `figma-design-to-code` before `get_design_context`)
2. Force output type **Bottom sheet** → `bottom_sheet_<feature>_<purpose>.xml` in `:presentation` `res/layout/`
3. If no Figma — create sheet XML using the rules below + shared Material rules from `figma-to-xml`

## Output

`:presentation` `res/layout/bottom_sheet_<feature>_<purpose>.xml`

Example: `bottom_sheet_filter_options.xml`, `bottom_sheet_compass_guidelines.xml`

## Structure

- Root `ConstraintLayout` or `LinearLayout` with top rounded corners (`bg_shape_*` top radius)
- Optional drag handle view
- Title `mtv`, content area, primary/secondary `mb`
- Scrollable content: `NestedScrollView` only if needed — keep shallow when possible

## Rules

- Material widgets; View Binding IDs (`mtvSheetTitle`, `mbSheetApply`)
- Clickable icons → `MaterialButton` IconButton (`ButtonStyle.IconButton` + `app:icon` + `android:padding="4dp"`), not clickable `siv`
- Button solid + stroke → tint / stroke / `cornerRadius` on the button — no `bg_shape_*` for that
- Clickable language/chip selectors → `MaterialButton` + Material background + `app:icon` end — not `MaterialTextView` + `bg_shape_*` / `drawableEnd`
- Portrait + landscape — avoid fixed heights that break landscape — unless `project-settings.json` says otherwise
- Strings from `:core-ui` `@string/`
- No Kotlin `BottomSheetDialogFragment` unless user requests separately
- Prefer `ParentSheet` / project base sheet when wiring later
- Closing tags: blank line between nested container closes; **no** extra blank line after the root closing tag

## vs dialog

- Use bottom sheet for contextual actions / filters / medium content
- Use `create-dialog` for small confirmations

## After layout

Tell user to wire via `create-mvi` or existing host if Kotlin not requested.
