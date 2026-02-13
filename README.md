# Pokedex

An Android Pokedex app built with Jetpack Compose and Material 3. Browse the first 150 Pokemon and view detailed information including types, abilities, descriptions, and type matchups.

## Features

- **Pokemon List** — Scrollable list showing all Pokemon with sprites, names, and IDs
- **Pokemon Detail** — Tap any Pokemon to see:
  - Official artwork
  - Type badges with color coding
  - Pokedex description
  - Abilities (with hidden ability indicators)
  - Type matchups (weaknesses, resistances, strengths, ineffective)

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
gradlew.bat test

# Clean build outputs
gradlew.bat clean
```

## Project Structure

```
app/src/main/java/com/cesar/pokedex/
├── MainActivity.kt                  # Entry point
├── PokedexApplication.kt            # Hilt application class
├── data/
│   ├── remote/
│   │   ├── PokeApiService.kt        # Retrofit API interface
│   │   └── dto/                     # API response data classes
│   └── repository/
│       └── PokemonRepositoryImpl.kt # Repository implementation
├── di/                              # Hilt modules
├── domain/
│   ├── model/                       # Domain models
│   └── repository/
│       └── PokemonRepository.kt     # Repository interface
└── ui/
    ├── navigation/
    │   └── PokedexNavHost.kt        # Navigation graph
    ├── screen/
    │   ├── pokemonlist/             # List screen + ViewModel
    │   └── pokemondetail/           # Detail screen + ViewModel
    └── theme/                       # Material 3 theme
```

## Architecture

The app follows a clean architecture pattern with three layers:

- **UI** — Compose screens with ViewModels exposing sealed UI state via `StateFlow`
- **Domain** — Models and repository interface
- **Data** — Retrofit API service, DTOs, and repository implementation

API calls are parallelized in the repository layer to minimize latency when loading Pokemon details.
