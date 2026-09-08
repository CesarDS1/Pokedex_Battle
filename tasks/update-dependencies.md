# Task: Update dependencies (safe patch/minor bumps)

## Goal

Bring `gradle/libs.versions.toml` up to date with the latest **patch/minor** releases of each
dependency, without pulling in major-version bumps that carry higher migration risk. Part of a
controlled process: gitflow branch (`feature/update-dependencies` off `develop`), this task file,
a PR into `develop`, and a `/code-review` pass before merging.

## Branch

`feature/update-dependencies` (base: `develop`)

## Scope: versions to update

| Catalog key | Current | New | Notes |
|---|---|---|---|
| hilt | 2.59.1 | 2.60.1 | patch |
| room | 2.7.1 | 2.8.4 | minor |
| navigationCompose | 2.8.3 | 2.10.0 | minor |
| coreKtx | 1.17.0 | 1.19.0 | minor |
| lifecycleRuntimeKtx | 2.10.0 | 2.11.0 | minor |
| activityCompose | 1.12.3 | 1.13.0 | minor |
| splashscreen | 1.0.1 | 1.2.0 | minor |
| hiltNavigationCompose | 1.2.0 | 1.4.0 | minor |
| lifecycleViewmodelCompose | 2.10.0 | 2.11.0 | minor |
| kotlinxSerialization | 1.7.3 | 1.11.0 | minor |
| coroutinesTest | 1.10.1 | 1.11.0 | minor |
| mockk | 1.13.16 | 1.14.11 | minor |
| turbine | 1.2.0 | 1.2.1 | patch |
| ksp | 2.3.6 | 2.3.11 | patch (kept within same 2.3.x line to stay paired with Kotlin 2.2.10) |
| firebaseCrashlytics (plugin) | 3.0.3 | 3.0.8 | patch |
| googleServices | 4.4.2 | 4.5.0 | minor |

## Already at latest (no change)

`retrofitKotlinxSerialization` (1.0.0), `coil` (2.7.0), `junit` (4.13.2), `junitVersion` (1.3.0),
`espressoCore` (3.7.0).

## Explicitly excluded from this task (major/high-risk — future follow-up task)

| Catalog key | Current | Latest seen | Why excluded |
|---|---|---|---|
| agp | 9.3.1 | 9.4.0 | user-requested exclusion, needs its own validation pass |
| kotlin | 2.2.10 | 2.4.0 | user-requested exclusion, language/compiler major bump |
| composeBom | 2026.06.01 | 2026.08.00 | user-requested exclusion, Compose runtime major bump |
| retrofit | 2.11.0 | 3.0.0 | major version, breaking API changes expected |
| okhttp | 4.12.0 | 5.5.0 | major version, breaking API changes expected |
| ktlintGradle | 12.1.2 | 14.2.0 | major version jump (12→14), likely config/rule changes |
| firebaseBom | 33.7.0 | 34.18.0 | major version, cross-SDK breaking changes possible |

## Checklist

- [x] Create `develop` branch from `master`, push to origin
- [x] Create `feature/update-dependencies` from `develop`
- [x] Write this task file
- [x] Apply version bumps to `gradle/libs.versions.toml`
- [x] `./gradlew testDebugUnitTest` passes (`BUILD SUCCESSFUL`, 36 tasks, all tests green)
- [x] `./gradlew build` — compilation succeeds (debug + release); the run fails at
      `runKtlintCheckOverKotlinScripts` with `com/pinterest/ktlint/rule/engine/core/api/RuleAutocorrectApproveHandler`.
      Confirmed via `git stash` that this failure is **pre-existing on `master`**, unrelated to
      this dependency bump (ktlintGradle stayed at 12.1.2, excluded from this task's scope) —
      out of scope for this PR.
- [x] Update `CLAUDE.md` — fixed stale Project Overview line (also corrected pre-existing drift:
      AGP 9.0.0→9.3.1, Java 11→17, Compile SDK 36→37, split Target/Compile SDK — these were
      already wrong on `master` from a prior undocumented bump, unrelated to this task's version
      changes)
- [x] Commit, push `feature/update-dependencies`
- [x] Open PR into `develop` via `gh pr create` — https://github.com/CesarDS1/Pokedex_Battle/pull/1
- [x] Run `/code-review` on the PR, address findings — 3 findings, all `CLAUDE.md` drift unrelated
      to the dependency bumps themselves (Room DB version 3→4, ktlint claimed absent but is
      configured, stale Compose BOM number). Fixed, plus one more found while fixing (release
      minification claimed disabled but is enabled).
