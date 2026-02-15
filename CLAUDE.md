# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Pokedex app built with Jetpack Compose and Material 3. Single-module project using Kotlin DSL Gradle build files. Fetches data from PokeAPI v2 with offline caching via Room.

- **Package:** `com.cesar.pokedex`
- **Min SDK:** 33, **Target/Compile SDK:** 36
- **Kotlin:** 2.2.10, **AGP:** 9.0.0, **Java:** 11
- **UI:** Jetpack Compose with Material 3 and dynamic color support

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on connected device/emulator
./gradlew test                   # Run unit tests (JVM)
./gradlew testDebugUnitTest      # Run debug unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device)
./gradlew test --tests "ExampleUnitTest"  # Run a single test class
./gradlew clean                  # Clean build outputs
```

## Architecture

Clean Architecture with 3 layers (UI, Domain, Data). Single-activity architecture using `ComponentActivity` with Compose. The app uses edge-to-edge display and Jetpack Navigation for screen transitions.

**Source layout:** `app/src/main/java/com/cesar/pokedex/`

```
├── MainActivity.kt                    — Entry point, sets up NavHost
├── PokedexApplication.kt              — Hilt application class
│
├── data/
│   ├── di/
│   │   └── DataModule.kt              — Hilt DI module (Retrofit, Room, Repository)
│   ├── local/
│   │   ├── PokedexDatabase.kt         — Room database class
│   │   ├── Converters.kt              — Room type converters
│   │   ├── dao/
│   │   │   └── PokemonDao.kt          — Data access object queries
│   │   └── entity/
│   │       ├── PokemonEntity.kt       — Room entity for Pokemon list
│   │       ├── PokemonDetailEntity.kt — Room entity for Pokemon details
│   │       ├── PokemonEvolutionEntity.kt — Room entity for evolution chains
│   │       └── FavoritePokemonEntity.kt — Room entity for favorite Pokemon
│   ├── remote/
│   │   ├── PokeApiService.kt          — Retrofit API interface (PokeAPI v2)
│   │   └── dto/                       — API response DTOs (kotlinx.serialization)
│   │       ├── PokemonListResponse.kt
│   │       ├── PokemonDetailResponse.kt
│   │       ├── PokemonSpeciesResponse.kt
│   │       ├── EvolutionChainResponse.kt
│   │       ├── MoveResponse.kt
│   │       ├── TypeResponse.kt
│   │       └── NamedApiResource.kt
│   └── repository/
│       └── PokemonRepositoryImpl.kt   — Repository implementation
│
├── domain/
│   ├── model/
│   │   ├── Pokemon.kt                 — Pokemon list domain model
│   │   ├── PokemonDetail.kt           — Pokemon detail domain model
│   │   └── PokemonEvolutionInfo.kt    — Evolution chain domain model
│   └── repository/
│       └── PokemonRepository.kt       — Repository interface
│
└── ui/
    ├── navigation/
    │   └── PokedexNavHost.kt           — Navigation graph (list, detail, moves, evolution)
    ├── screen/
    │   ├── pokemonlist/
    │   │   ├── PokemonListScreen.kt    — List screen composables
    │   │   └── PokemonListViewModel.kt
    │   ├── pokemondetail/
    │   │   ├── PokemonDetailScreen.kt  — Detail screen composables
    │   │   └── PokemonDetailViewModel.kt
    │   ├── pokemonmoves/
    │   │   ├── PokemonMovesScreen.kt   — Moves screen composables
    │   │   └── PokemonMovesViewModel.kt
    │   └── pokemonevolution/
    │       ├── PokemonEvolutionScreen.kt — Evolution screen composables
    │       └── PokemonEvolutionViewModel.kt
    └── theme/
        ├── Color.kt                    — Material 3 color definitions
        ├── Theme.kt                    — Material 3 theme setup
        └── Type.kt                     — Typography definitions
```

**Dependencies are managed via version catalog:** `gradle/libs.versions.toml`

## Key Libraries

| Library | Purpose |
|---------|---------|
| Jetpack Compose + Material 3 | UI framework |
| Navigation Compose | Screen navigation |
| Hilt | Dependency injection |
| Retrofit + OkHttp | HTTP networking |
| kotlinx.serialization | JSON parsing |
| Coil | Image loading |
| Room | Local database / offline caching |

## Testing

- **Unit tests:** `app/src/test/java/com/cesar/pokedex/` — JUnit 4 + MockK + Turbine, runs on JVM
  - `data/repository/PokemonRepositoryImplTest.kt`
  - `ui/screen/pokemonlist/PokemonListViewModelTest.kt`
  - `ui/screen/pokemonlist/GenerationOfTest.kt`
  - `ui/screen/pokemondetail/PokemonDetailViewModelTest.kt`
  - `ui/screen/pokemonevolution/PokemonEvolutionViewModelTest.kt`
  - `ui/screen/pokemonmoves/PokemonMovesViewModelTest.kt`
  - `util/MainDispatcherRule.kt` — Custom test dispatcher rule
- **Instrumented tests:** `app/src/androidTest/java/com/cesar/pokedex/` — Espresso + Compose testing, requires device/emulator
- **Test runner:** `androidx.test.runner.AndroidJUnitRunner`

## Key Configuration

- `kotlin.code.style=official` (in `gradle.properties`)
- Non-transitive R classes enabled (`android.nonTransitiveRClass=true`)
- Compose BOM `2024.09.00` — avoid `FlowRow` (API signature mismatch at runtime), use custom `WrappingRow` Layout composable instead (defined in `PokemonDetailScreen.kt`)
- Room database version: 3 (includes `favorites` table)
- No linter or formatter configured (no ktlint/detekt)
- ProGuard rules exist but minification is disabled

## Features

- **Favorites:** Pokemon can be favorited from the list (heart icon per item) or detail screen (heart icon in top bar). Favorites are persisted in Room via `FavoritePokemonEntity`. The list screen top bar has a toggle to show favorites only. Favorite state flows reactively from `PokemonRepository.getFavoriteIds(): Flow<Set<Int>>`.
- **Type filtering:** List screen has a horizontal scrollable `TypeFilterRow` with all 18 Pokemon types. Multiple types can be selected simultaneously. Type colors use `typeColor()` from `PokemonDetailScreen.kt`.
- **Swipeable detail tabs:** Detail screen uses `HorizontalPager` for About/Stats/Matchups tabs (swipe between tabs).
- **Pokemon info card:** Detail About tab shows height, weight, abilities (with hidden label), and gender ratio.
- **Clickable evolutions:** Evolution screen Pokemon cards navigate to their detail screen via `onPokemonClick` callback.
- **WrappingRow:** Custom `Layout` composable in `PokemonDetailScreen.kt` for wrapping type badges (replaces broken `FlowRow`).
