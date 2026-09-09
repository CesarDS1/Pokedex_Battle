# Task: Update dependencies — major/high-risk bumps (definition only)

## Goal

This is the follow-up to `tasks/update-dependencies.md` (PR #1, safe patch/minor bumps). It
documents the 7 dependencies that were intentionally excluded from that PR because they involve
major-version jumps. **This document only defines and researches each bump — none of them are
applied yet.** Each one will be tackled later as its own branch + PR off `develop`, verified with
both unit and instrumented tests.

Of the 7, only AGP, Kotlin and Compose BOM were the ones originally called out; Retrofit, OkHttp,
ktlint-gradle and firebase-bom were discovered as majors during the version research for PR #1.

## Suggested execution order (lowest risk → highest)

1. AGP
2. firebase-bom
3. Compose BOM
4. OkHttp
5. Retrofit
6. Kotlin
7. ktlint-gradle (last — most uncertain, and there's already a pre-existing broken ktlint task to untangle)

## Per-dependency definitions

### 1. AGP

| | |
|---|---|
| Catalog key | `agp` |
| Current → target | `9.3.1` → `9.4.0` |
| Risk | **Low** |
| Suggested branch | `feature/update-agp` |

Notes: AGP 9.4 adds a strict 1:1 flavor-dimension parity check between app and dynamic-feature
modules — this project is single-module with no dynamic features, so the check doesn't apply. No
other breaking changes found relevant to this project's setup.

### 2. firebase-bom

| | |
|---|---|
| Catalog key | `firebaseBom` |
| Current → target | `33.7.0` → `34.18.0` |
| Risk | **Low** |
| Suggested branch | `feature/update-firebase-bom` |

Notes: BOM v34 removed the `-ktx` modules from the BOM (KTX APIs should come from the main
modules now). Verified via grep — this project does **not** reference any `firebase-*-ktx`
artifacts (`firebase-crashlytics`/`firebase-analytics` are already the main modules), so this
doesn't apply here.

### 3. Compose BOM

| | |
|---|---|
| Catalog key | `composeBom` |
| Current → target | `2026.06.01` → `2026.08.00` (Compose 1.12) |
| Risk | **Low** |
| Suggested branch | `feature/update-compose-bom` |

Notes: Compose 1.12 requires AGP ≥ 9.1.2 (already satisfied even without the AGP bump above) and
`compileSdk` 37 (already at 37). Known deprecations: `Modifier.onFirstVisible()` (→
`onVisibilityChanged()`) and `Modifier.onFocusedBoundsChanged` (implementation removed). Verified
via grep — neither is used anywhere in `app/src/main`.

### 4. OkHttp

| | |
|---|---|
| Catalog key | `okhttp` |
| Current → target | `4.12.0` → `5.5.0` |
| Risk | **Low** |
| Suggested branch | `feature/update-okhttp` |

Notes: Ships separate JVM/Android artifacts (handled automatically via Gradle module metadata —
no action needed). Timeout configuration moved from `TimeUnit` params to `kotlin.time.Duration`
— verified via grep, this project does **not** call `callTimeout`/`readTimeout`/`writeTimeout`/
`connectTimeout` anywhere, so no call sites need updating. `MockWebServer` moved package/artifact
— not used in this project. Kotlin Multiplatform support was dropped — irrelevant, this is an
Android-only app.

### 5. Retrofit

| | |
|---|---|
| Catalog key | `retrofit` |
| Current → target | `2.11.0` → `3.0.0` |
| Risk | **Low-medium** |
| Suggested branch | `feature/update-retrofit` |

Notes: Retrofit 3.0 is a full Kotlin rewrite, requires OkHttp 4.12+ (do this **after** the OkHttp
bump above) and Java 8+/API 21+ (this project's `minSdk` is 33, fine). Mostly binary-compatible
with 2.x if using `Call<T>`; this project's `PokeApiService` uses coroutine `suspend` functions,
which is the modern path Retrofit 3.0 favors, so this should be low-friction. One behavior
change to watch: `Retrofit.create()` now has a non-null lower bound in Kotlin — shouldn't matter
here since `PokeApiService` is only used via Hilt-provided non-null instances.

### 6. Kotlin

| | |
|---|---|
| Catalog key | `kotlin` |
| Current → target | `2.2.10` → `2.4.0` |
| Risk | **Medium** |
| Suggested branch | `feature/update-kotlin` |

Notes: K1 compiler support is fully removed in 2.4.0 — this project already builds with K2
(default since Kotlin 2.0), so no action expected. The default annotation use-site target changes
when none is specified explicitly (now prefers `param`+`property` over `field`) — this project
relies heavily on constructor-injected annotations via Hilt (`@Inject`) and Room entities/DAOs,
which is exactly the pattern this default change affects. **Needs the most careful verification
of the 7**: full rebuild of DI graph and Room schema/queries, not just a version bump. `ksp` may
also need to move in lockstep with this bump (check KSP release compatible with Kotlin 2.4.x at
the time this is executed — do not reuse the `ksp = "2.3.11"` pin from PR #1, which was
deliberately kept paired with Kotlin 2.2.10).

### 7. ktlint-gradle

| | |
|---|---|
| Catalog key | `ktlintGradle` |
| Current → target | `12.1.2` → `14.2.0` |
| Risk | **Uncertain** |
| Suggested branch | `feature/update-ktlint-gradle` |

Notes: No specific 12→14 breaking-change details were found; historically, major ktlint-gradle
transitions have kept the declarative `ktlint { }` DSL block stable while refactoring the
underlying task classes. This project already has a **pre-existing, unrelated build failure** on
`runKtlintCheckOverKotlinScripts` (`com/pinterest/ktlint/rule/engine/core/api/RuleAutocorrectApproveHandler`,
confirmed present on `master` before PR #1 — see `tasks/update-dependencies.md`). This bump should
be evaluated together with that pre-existing failure — it's possible the newer plugin version
resolves it, or it may need a separate fix. Do this one last and expect to spend the most
exploratory time here.

## Checklist per dependency (repeat for each, when executed)

- [ ] Create branch `feature/update-<dep>` from `develop`
- [ ] Apply the version bump in `gradle/libs.versions.toml`
- [ ] `./gradlew testDebugUnitTest` passes
- [ ] `./gradlew connectedAndroidTest` passes (uses a running emulator/device — `emulator-5554`
      was available when this document was written)
- [ ] `./gradlew build` — note whether the pre-existing ktlint failure is still present/unrelated
- [ ] Update `CLAUDE.md` via the `docs` skill if the bump changes any documented fact
- [ ] Commit, push `feature/update-<dep>`
- [ ] `gh pr create --base develop`
- [ ] Run `/code-review` on the PR, address findings

## Status

All 7 dependencies: **not started**. This document is definition/research only.