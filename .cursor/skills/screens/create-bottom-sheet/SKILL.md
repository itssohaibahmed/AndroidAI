---
name: create-bottom-sheet
description: Create Android bottom sheet XML layout only (bottom_sheet_*.xml). Use when user asks for bottom sheet layout XML without Kotlin BottomSheetDialogFragment wiring.
---

# Create Bottom Sheet Layout (XML only)

Follow `.cursor/rules/09-resources-xml.mdc`.

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
- Portrait + landscape — avoid fixed heights that break landscape
- Strings from `:core-ui` `@string/`
- No Kotlin `BottomSheetDialogFragment` unless user requests separately
- Prefer `ParentSheet` / project base sheet when wiring later

## vs dialog

- Use bottom sheet for contextual actions / filters / medium content
- Use `create-dialog` for small confirmations
