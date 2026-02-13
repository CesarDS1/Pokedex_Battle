# Pokedex

An Android Pokedex app built with Jetpack Compose and Material 3. Browse all Pokemon across every generation with detailed information including types, abilities, stats, moves, evolution chains, and more.

## Features

- **Pokemon List** — Scrollable list of all Pokemon grouped by generation, with sprites, names, IDs, and type badges
  - Search by name or Pokedex number
  - Pull-to-refresh support
  - Collapsible generation sections
- **Pokemon Detail** — Tap any Pokemon to see:
  - Official artwork
  - Type badges with color coding
  - Pokedex description and region
  - Base stats
  - Abilities (with hidden ability indicators)
  - Type matchups (weaknesses, resistances, strengths, ineffective)
  - Pokemon cry playback
- **Moves** — Level-up moves sorted by level, with move type info
- **Evolution Chain** — Full evolution tree with evolution triggers (level, trade, item, etc.)
- **Varieties** — Alternate forms and mega evolutions
- **Offline Support** — Local Room database caching for previously viewed data

## Tech Stack

| Category              | Technology                          |
|-----------------------|-------------------------------------|
| Language              | Kotlin 2.2.10                       |
| UI                    | Jetpack Compose + Material 3        |
| Navigation            | Navigation Compose                  |
| Dependency Injection  | Hilt                                |
| Networking            | Retrofit + OkHttp                   |
| JSON Parsing          | kotlinx.serialization               |
| Image Loading         | Coil                                |
| Local Database        | Room                                |
| API                   | [PokeAPI v2](https://pokeapi.co/)   |

## Requirements

- Android Studio Ladybug or newer
- Min SDK 33 (Android 13)
- JDK 11+

## Building

```bash
# Build debug APK
gradlew.bat assembleDebug

# Install on connected device/emulator
gradlew.bat installDebug

# Run unit tests
gradlew.bat testDebugUnitTest

# Clean build outputs
gradlew.bat clean
```

## Project Structure

```
app/src/main/java/com/cesar/pokedex/
├── MainActivity.kt                  # Entry point
├── PokedexApplication.kt            # Hilt application class
├── data/
│   ├── local/
│   │   ├── PokedexDatabase.kt       # Room database
│   │   ├── dao/PokemonDao.kt        # Data access object
│   │   └── entity/                  # Room entities
│   ├── remote/
│   │   ├── PokeApiService.kt        # Retrofit API interface
│   │   └── dto/                     # API response data classes
│   ├── di/DataModule.kt             # Hilt data module
│   └── repository/
│       └── PokemonRepositoryImpl.kt # Repository implementation
├── domain/
│   ├── model/                       # Domain models
│   └── repository/
│       └── PokemonRepository.kt     # Repository interface
└── ui/
    ├── navigation/
    │   └── PokedexNavHost.kt        # Navigation graph
    ├── screen/
    │   ├── pokemonlist/             # List screen + ViewModel
    │   ├── pokemondetail/           # Detail screen + ViewModel
    │   ├── pokemonmoves/            # Moves screen + ViewModel
    │   └── pokemonevolution/        # Evolution screen + ViewModel
    └── theme/                       # Material 3 theme
```

## Architecture

The app follows a clean architecture pattern with three layers:

- **UI** — Compose screens with ViewModels exposing sealed UI state via `StateFlow`
- **Domain** — Models and repository interface
- **Data** — Retrofit API service, DTOs, Room database, and repository implementation

API calls are parallelized in the repository layer to minimize latency. Data is cached locally in Room so previously viewed Pokemon load instantly offline.

## Testing

Unit tests cover ViewModels and the repository layer using JUnit 4, MockK, and Turbine:

```bash
# Run all unit tests
gradlew.bat testDebugUnitTest

# Run a specific test class
gradlew.bat test --tests "PokemonListViewModelTest"
```
