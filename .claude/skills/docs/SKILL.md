---
name: Docs updater
description: Ensure that every time a feature is added or modified in the Pokedex Android app, CLAUDE.md is updated to reflect the new architecture, files, libraries, tests, and features.
---

# Skill: CLAUDE.md Documentation Updater

## Purpose

Keep `CLAUDE.md` in sync with the codebase. Whenever a feature is added or modified, update the relevant sections of `CLAUDE.md` so it stays an accurate map of the project.

---

## Instructions

Whenever you perform ANY of the following:

* Add a new screen, ViewModel, or navigation route
* Add or modify a Room entity, DAO, or bump the database version
* Add a new dependency to `gradle/libs.versions.toml`
* Add or modify a user-facing feature
* Add new test files

You MUST update `CLAUDE.md` accordingly. This rule is mandatory and cannot be skipped.

Do NOT wait to be asked separately — updating `CLAUDE.md` is part of finishing the change, the same way updating tests is.

---

## What to update, section by section

### 1. Architecture / directory tree

Add a line for each new file, in the correct location in the tree, with a short trailing description comment matching the existing style:

```
│   └── newfile.kt                 — Short description
```

Keep alignment of the `—` comments reasonably consistent with neighboring lines. Do not reformat unrelated parts of the tree.

### 2. Key Libraries table

If a new dependency was added to the version catalog, add a row: `| Library | Purpose |`. Skip this if no dependency changed.

### 3. Testing section

Add the new test file path to the correct bullet list (`Unit tests` or `Instrumented tests`), following the existing format.

### 4. Features section

Add a new bullet: `**Feature Name:** description of what it does and where it lives (key files/composables).` Match the tone and length of existing bullets (Favorites, Type filtering, Swipeable detail tabs, etc.).

### 5. Key Configuration

Only touch this if the change affects it directly (e.g., Room DB version bump, new Gradle flag, Compose BOM change). Do not touch it otherwise.

---

## What NOT to do

* Do not rewrite whole sections — make the smallest edit that keeps the doc accurate.
* Do not invent or describe features that are not actually implemented.
* Do not touch `Build Commands` or the Kotlin/AGP/SDK version table unless the change directly modifies them.
* Do not duplicate information already covered by an existing bullet — extend it instead of adding a near-duplicate.

---

## Output

Show the edit made to `CLAUDE.md` (via the normal edit diff) alongside the code change, so the user can review both together.

---

End of skill.
