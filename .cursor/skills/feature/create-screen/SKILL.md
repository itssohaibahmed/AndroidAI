---
name: create-screen
description: Full screen scaffold in one invoke — UI (figma-to-xml or figma-to-compose) + create-mvi + create-clean-architecture when domain/data is needed. Use when the user wants a complete feature screen without attaching the three skills separately. Do not use for ads / :gmaAds, dialogs-only, or bottom-sheets-only (use create-dialog / create-bottom-sheet).
---

# Create Screen (orchestrator)

One skill for a **complete feature screen**: layout/UI → optional domain/data → presentation MVI.

**Does not replace** the leaf skills. It **loads and runs** them in order. Prefer this when the user would otherwise attach `figma-to-xml` / `figma-to-compose` + `create-mvi` + `create-clean-architecture`.

Follow `.cursor/rules/` — especially `00-global`, `01-feature-checklist`, and whatever each leaf skill lists.

Obey `.cursor/project-settings.json` when present (`uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`).

## When to use

| Use **`create-screen`**                           | Use a leaf skill instead                                         |
|---------------------------------------------------|------------------------------------------------------------------|
| New feature screen end-to-end (Figma or freeform) | Layout only → `figma-to-xml` / `figma-to-compose`                |
| User asked for “full screen” / “wire everything”  | MVI only (layout exists) → `create-mvi`                          |
| Domain + UI + MVI in one chat                     | Domain/data only → `create-clean-architecture`                   |
|                                                   | Dialog / sheet UI only → `create-dialog` / `create-bottom-sheet` |

**Never** use for ads / `:gmaAds` / AdMob (`21-ads-billing`) unless the user **explicitly** asks to convert ads to MVI.

## Preconditions

Confirm before coding:

1. **Feature name** (camelCase folder, e.g. `userProfile`)
2. **Figma URL** (optional) or freeform layout brief
3. **New domain/data?** — yes if new UseCase / repository / DataSource / API / Room is required; no if injecting existing UseCases only
4. Read **`uiFramework`** from `project-settings.json` (default `xml`)

## Orchestration order (mandatory)

Execute **in this order**. For each step: **read that skill’s `SKILL.md` and follow it fully** — do not invent a parallel checklist.

### 1 — UI

| `uiFramework`   | Run skill                                                              |
|-----------------|------------------------------------------------------------------------|
| `xml` (default) | **`figma-to-xml`** (`.cursor/skills/ui/figma-to-xml/SKILL.md`)         |
| `compose`       | **`figma-to-compose`** (`.cursor/skills/ui/figma-to-compose/SKILL.md`) |

- Dashboard + BottomNavigation → produce menu + `nav_graph_dashboard` as that skill requires
- Dialog-only / sheet-only requests → stop and redirect to `create-dialog` / `create-bottom-sheet` (do not continue MVI here unless the user also asked for a host screen)

### 2 — Domain + data (conditional)

If the screen needs **new** UseCases, repository interfaces/impls, DataSources, Retrofit, Room, or prefs wiring:

→ Run **`create-clean-architecture`** (`.cursor/skills/feature/create-clean-architecture/SKILL.md`)

Skip this step when only existing domain APIs are reused.

### 3 — Presentation MVI

→ Run **`create-mvi`** (`.cursor/skills/feature/create-mvi/SKILL.md`)

Wire Fragment / `*Screen`, Intent / State / Effect / ViewModel, DI, nav destination, and tests per that skill + `writeTestsWithFeatures`.

## After all steps

- Walk **`01-feature-checklist`**
- Summarize what was created (layouts, domain/data files if any, MVI package, nav)
- Point to `/review-complete` before PR when appropriate

## Do not

- Duplicate leaf-skill bodies inside this file — always **open and follow** the linked `SKILL.md`
- Skip `create-clean-architecture` when new domain/data is clearly required, then invent repos inside `create-mvi`
- Create presentation → `:data` dependencies
- Run only UI and stop when the user asked for a full screen (unless they cancel mid-flow)
