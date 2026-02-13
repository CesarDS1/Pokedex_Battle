# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Pokedex app built with Jetpack Compose and Material 3. Single-module project using Kotlin DSL Gradle build files. Fetches data from PokeAPI v2.

- **Package:** `com.cesar.pokedex`
- **Min SDK:** 33, **Target/Compile SDK:** 36
- **Kotlin:** 2.2.10, **AGP:** 9.0.0, **Java:** 11
- **UI:** Jetpack Compose with Material 3 and dynamic color support

## Build Commands

```bash
# Windows (use gradlew.bat, not ./gradlew)
gradlew.bat assembleDebug          # Build debug APK
gradlew.bat installDebug           # Install on connected device/emulator
gradlew.bat test                   # Run unit tests (JVM)
gradlew.bat testDebugUnitTest      # Run debug unit tests
gradlew.bat connectedAndroidTest   # Run instrumented tests (requires device)
gradlew.bat test --tests "ExampleUnitTest"  # Run a single test class
gradlew.bat clean                  # Clean build outputs
```

## Architecture

Single-activity architecture using `ComponentActivity` with Compose. The app uses edge-to-edge display and Jetpack Navigation for screen transitions.

**Source layout:** `app/src/main/java/com/cesar/pokedex/`

- `MainActivity.kt` — Entry point, sets up NavHost
- `ui/navigation/PokedexNavHost.kt` — Navigation graph (list and detail routes)
- `ui/screen/pokemonlist/` — Pokemon list screen + ViewModel
- `ui/screen/pokemondetail/` — Pokemon detail screen + ViewModel
- `ui/theme/` — Material 3 theme (Color, Theme, Type definitions)
- `data/remote/PokeApiService.kt` — Retrofit API interface (PokeAPI v2)
- `data/remote/dto/` — API response DTOs (serialized with kotlinx.serialization)
- `data/repository/PokemonRepositoryImpl.kt` — Repository implementation
- `domain/model/` — Domain models (`Pokemon`, `PokemonDetail`, `PokemonType`, `Ability`)
- `domain/repository/PokemonRepository.kt` — Repository interface
- `di/` — Hilt dependency injection modules

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

## Testing

- **Unit tests:** `app/src/test/java/com/cesar/pokedex/` — JUnit 4, runs on JVM
- **Instrumented tests:** `app/src/androidTest/java/com/cesar/pokedex/` — Espresso + Compose testing, requires device/emulator
- **Test runner:** `androidx.test.runner.AndroidJUnitRunner`

## Key Configuration

- `kotlin.code.style=official` (in `gradle.properties`)
- Non-transitive R classes enabled (`android.nonTransitiveRClass=true`)
- Compose BOM `2024.09.00` — avoid `FlowRow` (API signature mismatch at runtime), use `Row` instead
- No linter or formatter configured (no ktlint/detekt)
- ProGuard rules exist but minification is disabled
