# UI/UX Advisor — Agent Memory

See detailed notes in topic files linked below.

## Key Files
- Detail screen: `ui/screen/pokemondetail/PokemonDetailScreen.kt`, `StatsTab.kt`, `MatchupsTab.kt`
- Shared components: `ui/component/TypeComponents.kt` (TypeBadge, WrappingRow, typeColor)
- Theme: `ui/theme/Theme.kt` (dynamic color on API 31+), `ui/theme/Color.kt` (fallback purple palette)
- Domain model: `domain/model/PokemonDetail.kt`

## Confirmed Patterns & Conventions
- **WrappingRow**: Custom Layout in TypeComponents.kt — always use instead of FlowRow
- **typeColor()**: Defined in TypeComponents.kt, returns Color per type name (lowercase)
- **TypeBadge**: Box + background(typeColor) + white labelMedium text, 16dp corner radius
- **ElevatedCard**: Used consistently for content sections (info card, stats, description, matchups)
- **Spacing**: 16dp horizontal screen padding, 12dp vertical section gaps, 8dp intra-section gaps
- **Dynamic color**: Enabled by default (Android 12+); fallback is a generic purple scheme
- **Stat bar scale**: 255 is max (progress = baseStat / 255f)
- **Gender encoding**: genderRate -1=genderless, 0=100% male, 8=100% female, else femalePercent = rate * 12.5

## Known Issues / Design Debt (from first review)
See `patterns.md` for full prioritized list.
- Hero image has no placeholder/error state (AsyncImage with no placeholder)
- TopAppBar title capitalisation: raw API name (e.g. "bulbasaur") not capitalised
- Name + ID on same line makes hierarchy unclear; Pokémon number not visually distinct
- stat bar color is a single `primary` color — no semantic coloring by value
- No animated entrance for stat bars
- `formatGender` returns a plain string — no visual gender ratio bar
- `isPlayingCry` state has no animated transition (abrupt icon ↔ spinner swap)
- GamesTab cards: text can overlap Pokéball decoration on narrow titles
- About tab navigation buttons use generic ArrowForward/List icons — weak affordance
- No scroll position memory when switching tabs (each tab resets scroll)
- Header area (image + name + region) not themed to Pokémon type color
