# AndroidAI Cursor template — todo (→ 100%)

Goal: one company-grade Cursor template that 10 developers can reuse so **code style, architecture, and AI workflows stay in sync** across all apps.

Use this as the roadmap. Check items off as they land; keep a short changelog when you cut a template version.

---

## 1. Source of truth & distribution

- [ ] Treat **this repo’s `.cursor/`** as the single template (stop hand-copying into Speak-Translate / other apps forever)
- [ ] Decide distribution model (pick one):
  - [ ] **A)** Dedicated `android-cursor-template` repo + copy/script into apps
  - [ ] **B)** Git submodule / subtree under each app’s `.cursor`
  - [ ] **C)** Internal package / zip release (`v1.x`) apps pin
- [ ] Add `VERSION` (or tag) + `CHANGELOG.md` for every template release
- [ ] Add a sync script (e.g. `scripts/sync-cursor-template.ps1` / `.sh`) that pulls a tagged version into a target app
- [ ] Document “how to update an app’s `.cursor`” in 5 steps for juniors
- [ ] CI or checklist: apps must not diverge silently (hash / version file check optional)

---

## 2. Rules vs Skills vs Commands (clarity)

- [ ] Add a short `.cursor/README.md` explaining:
  - **Rules** = company law (always / glob / agent-requestable)
  - **Skills** = multi-step playbooks (+ templates)
  - **Commands** = legacy slash macros → prefer Skills with `/` invoke (optional `disable-model-invocation: true` for human-only)
- [ ] Do **not** invent a large `.cursor/commands/` tree unless needed; implement slash workflows as **skills**
- [ ] Audit every rule: set correct `alwaysApply` / `globs` / description (agent-requestable)
- [ ] Keep **only** `00-global` + `01-ai-agent` (and maybe security/logging one-liners) as `alwaysApply: true`
- [ ] Move long procedural content out of rules into skills (rules stay short invariants + BAD/GOOD)
- [ ] Deduplicate overlapping bullets across `01`, `04`, `09`, `12`, `13`, `19` (one canonical home + cross-links)

---

## 3. Rules quality pass

- [ ] Frontmatter audit: every rule has clear `description`; globs match real paths
- [ ] Split or trim rules that are too long for reliable agent use (target: lean always-on; depth on demand)
- [ ] Ensure every “Never” has a matching Detekt/Lint/CI check where possible (see §6)
- [ ] Fill gaps if missing or thin:
  - [ ] Analytics / `EventsProvider` conventions (beyond naming)
  - [ ] Localization / RTL beyond anim-ldrtl
  - [ ] Accessibility (`cd_*`, touch targets, TalkBack)
  - [ ] Dark theme / `values-night` conventions
  - [ ] ProGuard/R8 keep rules for MVI packages (checklist completeness)
  - [ ] Deep links end-to-end (graph + manifest + validation)
  - [ ] WorkManager / background jobs (if company uses them)
  - [ ] Multi-flavor / product flavors conventions (if needed)
- [ ] Resolve contradictions (e.g. MTV compound drawables vs MaterialButton chips; icon height wrap vs fixed tap targets) — one official line each
- [ ] Add “decision table” for common UI choices (icon button / chip / card / list row)

---

## 4. Skills completeness

- [ ] Skill descriptions: must state **when to use** so the agent auto-picks correctly
- [ ] Every skill references the right rules (no orphan workflows)
- [ ] `setup-new-project`: end-to-end dry run on a blank module; fix broken template paths
- [ ] Parent*/Base* templates: match live Speak-Translate / production apps 1:1 (no stale APIs)
- [ ] `create-mvi`: generate Intent/State/Effect/VM/Fragment/DI/tests consistently with `19-base-ui` (inline clicks, `navArgs`, etc.)
- [ ] Screen skills (`create-screen` / dialog / bottom-sheet): align with latest XML EOF + MaterialButton surface rules
- [ ] `figma-to-xml`: mandatory checklist (SVG prefer, no `bg_shape` for button solid+stroke, closing-tag format)
- [ ] Data skills (Room / Retrofit / prefs): require version-catalog + human approval for new libs
- [ ] `architecture/review` + `quality/*`: runnable as a PR review ritual
- [ ] `testing/unit-tests`: fake repos + ViewModel/UseCase patterns match `11-testing`
- [ ] `pre-release`: map to real Play / version / mapping file steps your company uses
- [ ] Missing skills to add (as needed):
  - [ ] `/new-feature` or skill: full feature from empty (XML + MVI + nav + DI + strings)
  - [ ] `/arch-review` human-triggered skill (`disable-model-invocation: true` if slash-only)
  - [ ] `/sync-template` — bump `.cursor` from company template version
  - [ ] Ads / billing feature skill (if `21-ads-billing` is used often)
  - [ ] Firebase / Remote Config feature skill
  - [ ] Migration skill: “bring legacy screen to MVI”

---

## 5. Team slash workflows (Skills, not old Commands)

- [ ] Define 4–6 official slash skills everyone learns day 1:
  - [ ] `new-mvi` / `new-screen`
  - [ ] `arch-review`
  - [ ] `pre-release`
  - [ ] `figma-to-xml` (already exists — document `/` usage)
  - [ ] `sync-cursor-template`
- [ ] One-pager in onboarding: “type `/` and pick …”
- [ ] Optionally run Cursor `/migrate-to-skills` if any personal commands exist

---

## 6. Non-AI enforcement (required for “100%”)

Rules teach the agent; CI protects the repo.

- [ ] Detekt (or ktlint) aligned with Never list: no Compose, no `findViewById`, package/module boundary smells where possible
- [ ] Android Lint baselines + hard fails for secrets / exported components
- [ ] PR template checklist mirroring `01-ai-agent` feature checklist
- [ ] CODEOWNERS for `.cursor/` (tech lead only merges template changes)
- [ ] Pre-commit or CI: block commit of `local.properties`, keystores, `.env`
- [ ] Optional: architecture test (Konsist) for `presentation` ↛ `data`

---

## 7. Onboarding & governance

- [ ] `CONTRIBUTING.md`: point to `.cursor/README.md` + “update the template, don’t invent local rules”
- [ ] Onboarding exercise: new hire adds a dummy feature via `create-mvi` + screen skill
- [ ] Weekly convention PR: every real review finding → rule/skill update same week
- [ ] Named owner + backup for the template
- [ ] Team agreement: no personal User Rules that contradict project rules (or document allowed overrides)

---

## 8. Consistency cleanup (known / recent conventions)

Verify all rules, skills, and Parent* templates enforce:

- [ ] Inline clicks in `onViewCreated` — no `setupClicks()`
- [ ] Safe Args property name `navArgs` (not `args`)
- [ ] Language/chip selectors = `MaterialButton` + end icon (no MTV + `bg_shape`)
- [ ] Button solid+stroke = tint / stroke / `cornerRadius` (no oval `bg_shape`)
- [ ] DiffUtil simple overrides = one-liners
- [ ] XML: blank line between nested container closes; no extra blank after root
- [ ] Predictive back / `OnBackPressedDispatcher` only when feature asks
- [ ] `lazyModule` everywhere; domain owns UseCases + repo interfaces
- [ ] Glide via `loadImage` — not `setImageResource` for dynamic images
- [ ] Collectors on `viewLifecycleOwner`; nav via `navigateTo` / `popFrom`

---

## 9. Validation / dogfooding

- [ ] Generate one greenfield app from `setup-new-project` and ship a sample feature
- [ ] Port one real screen from Speak-Translate using only skills (no manual “fixups”)
- [ ] Have 2–3 other developers try the same prompts; collect failures → update rules
- [ ] Measure: fewer style comments on PRs after 2 sprints
- [ ] Spot-check agent output for EOF XML, DiffUtil one-liners, `navArgs`, MaterialButton chips

---

## 10. Docs & polish

- [ ] `.cursor/README.md` — map of rules + skills + when to use what
- [ ] Architecture diagram (modules + MVI) linked from `00` / `03`
- [ ] “Forbidden libraries” and “approved stack” single table (`13-libraries-stack`) kept current
- [ ] Remove dead / outdated template files
- [ ] Ensure no secrets or app-specific IDs inside template skills
- [ ] Decide: keep this `todo.md` until done, then archive to `CHANGELOG` / docs

---

## Definition of done (“100%”)

- [ ] One versioned template; all company Android apps on the same major version
- [ ] New feature can be scaffolded by a junior via `/` skills with minimal review nits
- [ ] CI blocks the worst Never-list violations without relying on the AI
- [ ] No manual dual-repo `.cursor` copy drift
- [ ] Team can explain Rules vs Skills in one sentence each
- [ ] Template owner merges convention updates weekly from real PR feedback

---

## Suggested order (first 2 weeks)

1. `.cursor/README.md` + Rules/Skills policy  
2. `alwaysApply` / globs audit + dedupe  
3. Version + changelog + sync script  
4. Slash skills: `new-mvi`, `arch-review`, `sync-cursor-template`  
5. Detekt/CI + CODEOWNERS for `.cursor`  
6. Dogfood on one real app; fix gaps from §8  

---

*Living document — update checkboxes as work lands. Cut a template release when a milestone group is done.*
