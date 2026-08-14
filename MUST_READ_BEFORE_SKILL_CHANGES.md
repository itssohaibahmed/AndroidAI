# AndroidAI — Agent guide (attach this first)

**Read this file before changing anything under `.cursor/`.**

This repo is the **company Cursor template** for Clean Architecture Android apps (XML + View Binding, MVI, Koin `lazyModule`). Apps copy `.cursor/` into their project. Keep this repo the **single source of truth**.

Full map of existing rules/skills: [`.cursor/README.md`](.cursor/README.md)

---

## What lives where

| Thing                   | Path                                                  | Use for                                                   |
|-------------------------|-------------------------------------------------------|-----------------------------------------------------------|
| **Rules**               | `.cursor/rules/*.mdc`                                 | Standing law (architecture, naming, invariants)           |
| **Long detail**         | `.cursor/rules/reference/*.md`                        | Full examples / tables (linked from short `.mdc` stubs)   |
| **Skills**              | `.cursor/skills/**/SKILL.md`                          | Multi-step playbooks (`/skill-name` or agent auto-pick)   |
| **Project settings**    | `.cursor/project-settings.json`                       | Per-app knobs (tests, orientation, theme, app id)         |
| **Bootstrap templates** | `.cursor/skills/project/setup-new-project/templates/` | Parent*/Base* Kotlin + anim XML                           |

**Rules** = “always do it this way.”  
**Skills** = “when I ask, follow these steps.”

Do **not** grow a large `.cursor/commands/` tree — prefer skills with `/` invoke.

---

## Hard rules when editing this template

1. **Do not delete rule meaning.** Prefer move / merge / link. If text must leave a `.mdc`, put the **full body** in `rules/reference/` and leave a short stub + link.
2. **Do not invent a second stack** (Compose, Hilt, Data Binding, RxJava) unless the user explicitly asks.
3. **Obey existing patterns** in `.cursor/rules/` — especially `00-global.mdc`.
4. **Unique skill `name:`** — never two skills with the same `name`.
5. **Update** [`.cursor/README.md`](.cursor/README.md) skill map / rules index when you add or rename something.
6. Skills that create features/UI/tests must **read and obey** `.cursor/project-settings.json` when present.
7. **New product skills** must be based on reference apps (see below) and **shown to the user for acceptance** before any skill files are written.

---

## Always-on vs not

| File                       | Always-on? | Role                                 |
|----------------------------|------------|--------------------------------------|
| `00-global.mdc`            | Yes        | Stack + Always/Never                 |
| `14-security-secrets.mdc`  | Yes        | Secrets / exported / PII             |
| `16-logging.mdc`           | Yes        | `Constants.TAG*` format              |
| `01-feature-checklist.mdc` | **No**     | Checklist when scaffolding a feature |
| Other `02`–`26`            | **No**     | Glob or description when relevant    |

**Prefer fewer always-on rules.** New “must always” items → add to `00-global` only if truly every chat needs them; otherwise use globs or a skill.

---

## How to add or update a **rule**

### Small invariant (fits in one screen)

1. Edit the matching `.mdc` under `.cursor/rules/` (or add `NN-topic.mdc`).
2. Frontmatter:
   ```yaml
   ---
   description: Short trigger text for Agent
   globs: ["**/path/**/*.kt"]   # or omit if alwaysApply
   alwaysApply: false
   ---
   ```
3. Keep body short: invariants + BAD/GOOD. Point to `reference/` for long tables.

### Large / detailed rule (examples, long tables)

1. Put **full text** in `.cursor/rules/reference/<topic>.md`.
2. Keep `.mdc` as a **short stub**: must-follow bullets + link to that reference file.
3. Same pattern as `09-resources-xml.mdc` → `reference/resources-xml.md`.

### Naming

- Numbered rules: `00`–`26` style (`26-data-persistence.mdc`).
- Next free number if adding a new top-level topic.
- Reference files: kebab-case (`mvi-presentation.md`).

### Globs

- Prefer **narrow** paths (`**/presentation/**/*.kt`, `**/data/**/*.kt`) over `**/*.kt`.
- Keep a strong `description` so Agent can still load the rule when relevant.

---

## How to add or update a **skill**

### Folder layout

```
.cursor/skills/<area>/<skill-folder>/SKILL.md
```

Areas already used: `project`, `feature`, `ui`, `review`, `test`, `gradle`, `platform`, `premium`, `release`.

### SKILL.md shape

```markdown
---
name: my-skill-name
description: What it does + when to use it (Agent reads this to auto-pick).
---

# Title

Follow `.cursor/rules/…` (and `reference/…` when detail lives there).

Obey `.cursor/project-settings.json` when present.

## Steps

1. …
```

### Pick the right area

| You want to…                                        | Prefer                                                         |
|-----------------------------------------------------|----------------------------------------------------------------|
| Scaffold a screen (Intent/State/Effect/VM/Fragment) | `feature/create-mvi` (extend or new skill under `feature/`)    |
| Add domain/data/repo                                | `feature/create-clean-architecture`                            |
| XML / Figma layout only                             | `ui/figma-to-xml` (or dialog / bottom-sheet)                   |
| Review PR / architecture / perf / security          | `review/review-*` (+ wire into `review-complete` if full gate) |
| Unit / integration / E2E tests                      | `test/test-*` (+ `test-complete` if full suite)                |
| Gradle catalog / organize                           | `gradle/`                                                      |
| Ship checklist                                      | `release/pre-release`                                          |
| In-app billing / subscriptions / premium            | `premium/implement-in-app-billing`, `premium/add-subscription-packages`, `premium/add-inapp-packages` |
| New multi-step product feature (ads, IAP, …)        | New skill under a clear area; add **rules** for invariants     |

### Skill vs rule (quick)

- Repeated **constraint** → **rule** (and `reference/` if long).
- Repeated **workflow** (“do steps 1–7”) → **skill**.
- Do **not** copy full XML/Material rules into every UI skill — **link** `09` + `reference/resources-xml.md`.

### After adding a skill

1. Add a line to the skill map in [`.cursor/README.md`](.cursor/README.md).
2. If it is a review/test “complete” orchestrator, update that skill’s checklist to call the new one.

---

## New product / platform skills — learn from reference apps first

When the user asks to **create a new skill** for a real product feature (examples: `implement-in-app-update`, `implement-in-app-review`, billing, ads helpers, etc.), **do not invent the flow from scratch**.

### Reference apps (search in this order)

These are production apps. Use them as the working template source:

| Priority      | Path                                                                       |
|---------------|----------------------------------------------------------------------------|
| **1 (first)** | `E:\SohaibAhmed\UnderWorking\Qibla-Finder-Qibla-Compass-HSAIAppsLab`       |
| **2**         | `E:\SohaibAhmed\UnderWorking\MusicplayerPlayMP3Music-DigitalGenerationHub` |
| **3**         | `E:\SohaibAhmed\UnderWorking\Photo-Collage-Maker-HSAIAppsLab`              |

1. Search **ref 1** for the feature (classes, managers, Fragments, DI, Gradle).
2. If missing or unclear → search **ref 2**, then **ref 3**.
3. Prefer the **highest-priority app that has a complete, working implementation**.

### Mandatory acceptance before writing the skill

**Stop and ask the user before creating any skill files.** Show a short proposal and wait for explicit acceptance.

Include at least:

1. **Which reference app** you used (path + priority).
2. **What you found** — key files (manager, host screen, DI, Gradle deps).
3. **Proposed skill** — `name`, folder (`platform/…`, `feature/…`, etc.), and high-level steps.
4. **Where to implement in apps** — e.g. host screen (`DashboardFragment`), module (`:core-ui` manager + `:presentation` host). Confirm this with the user.
5. Ask clearly: **“Accept this skill plan? (yes / change …)”**

Only after the user accepts → write `SKILL.md` (put the agreed host screen / modules **inside the skill**), any templates under that skill folder, rules/reference if needed, and update `.cursor/README.md`.

### Workflow checklist (new skill from refs)

1. Read this README + `.cursor/README.md` + `00-global`.
2. Search ref apps **1 → 2 → 3** for a working implementation.
3. Draft proposal (template summary + host screen + modules).
4. **Ask user for acceptance** — do not write skill files yet.
5. On accept: create skill (+ templates if useful), optional rule stub, update skill map.
6. On reject/change: revise proposal and ask again.

---

## Typical work you will be asked for

### New feature conventions

1. Read `00-global`, `01-feature-checklist`, `04` / `reference/mvi-presentation`, `03`, `07` / DI reference.
2. Prefer extending existing skills (`create-mvi`, `create-clean-architecture`) over parallel skills.
3. Add a **rule** only for new invariants; add a **skill** only for a new multi-step flow.

### Updates (deps, Gradle, platform)

1. Prefer updating `gradle/gradle-update`, `gradle/gradle-organize`, or `08` / `reference/gradle.md`.
2. **`gradle-update`** must bump catalog **and** hardcoded `"g:a:v"` lines, migrate them into `libs.versions.toml`, and place under organize section headers.
3. Library allow/deny list → `13-libraries-stack.mdc`.
4. No new libraries without explicit human approval (`00-global`) — migrating an existing hardcode into the catalog is required, not a new library.

### Testing

1. Extend `test/test-unit`, `test-integration`, `test-e2e`, or `test-complete`.
2. Conventions → `11-testing.mdc` (pyramid: unit = fakes/Flow; integration = Room/MockWebServer; e2e = device UI).
3. Each `test-*` skill starts with a one-line `We are going to …` banner; device-needed skills must mention emulator/physical device (or already attached).
4. Authoring skills **write missing tests, execute, and report**. On failures: **ask user consent** before fixing production code, then retest. Do not silently weaken assertions.
5. Respect `project-settings.json` → `writeTestsWithFeatures`.
6. After rename/add of test skills, update the skill map in [`.cursor/README.md`](.cursor/README.md).

### Review

1. Extend `review/review-architecture`, `review-performance`, `review-security`, or `review-complete`.
2. Keep Pass/Fail report style consistent with existing review skills.

---

## Do / Don’t

**Do**

- Read `.cursor/README.md` + matching existing rule/skill before writing.
- For **new product skills**, search reference apps (Qibla → Music Player → Photo Collage) and **get user acceptance** before writing files.
- Put agreed host screen / modules **inside the skill** after acceptance.
- Link to `reference/` instead of duplicating long text.
- Keep skill `description` specific (when to use + when not to).
- Use simple wording in new docs.

**Don’t**

- Invent platform/feature flows when a reference app already has a working one.
- Write a new skill without showing the plan and waiting for acceptance.
- Delete reference bodies or rule lines “to clean up” without moving them.
- Add always-on rules for niche topics.
- Create duplicate skill names or a second skill tree (`screens/` vs `ui/`, etc.).
- Put Retrofit/Room/prefs as new skills — use `26-data-persistence` + `reference/`.
- Change Speak-Translate or other apps unless the user asks to sync.

---

## Suggested prompt when attaching this file

> Read `README.md` (this guide) and `.cursor/README.md` first. Then: \<your task\>. Follow the template rules: no content loss, prefer move/link, update the skill map if you add skills. For new product skills, search the reference apps (priority order), show me the plan (template + host screen), wait for my acceptance, then create the skill.

---

## Quick paths

| Need                | Open                                              |
|---------------------|---------------------------------------------------|
| Template overview   | `.cursor/README.md`                               |
| Always-on law       | `.cursor/rules/00-global.mdc`                     |
| Feature checklist   | `.cursor/rules/01-feature-checklist.mdc`          |
| XML detail          | `.cursor/rules/reference/resources-xml.md`        |
| MVI detail          | `.cursor/rules/reference/mvi-presentation.md`     |
| DI detail           | `.cursor/rules/reference/dependency-injection.md` |
| App settings schema | `.cursor/project-settings.json`                   |
