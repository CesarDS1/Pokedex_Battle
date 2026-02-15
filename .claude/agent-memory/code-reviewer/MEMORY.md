# Code Reviewer Memory

## Project Architecture Patterns

### Navigation Callbacks
- Navigation callbacks follow the pattern: `onXClick: (Int) -> Unit` for Pokemon ID navigation
- Callbacks are threaded through the composable hierarchy (Screen -> Content -> Card)
- NavHost wires callbacks to `navController.navigate("route/$id")`

### Screen Structure Pattern
- Screens use `@OptIn(ExperimentalMaterial3Api::class)` when using Material3 APIs
- Screen composables accept: `onBackClick`, navigation callbacks, optional `modifier`, `viewModel = hiltViewModel()`
- UI state collected with: `val uiState by viewModel.uiState.collectAsState()`
- Three-state pattern: Loading (CircularProgressIndicator), Error (message + retry button), Success (content)

### Compose UI Conventions
- Private composables for screen-internal UI (e.g., `EvolutionContent`, `VarietyCard`)
- Cards use `ElevatedCard` from Material 3
- Clickable cards: `.clickable(onClick = onClick)` modifier on Card
- Current item highlighting: conditional `.border()` modifier applied via `.then()`
- TypeBadge composable is `internal` and reused across screens (defined in PokemonDetailScreen)

### Custom Layout Workaround
- FlowRow causes runtime crashes with BOM 2024.09.00 (API signature mismatch)
- Use custom `WrappingRow` Layout composable instead for wrapping badge lists
- WrappingRow implements manual row-wrapping logic with configurable spacing

### Naming Conventions
- Domain models: `PokemonEvolutionInfo`, `EvolutionStage`, `PokemonVariety`
- Navigation callbacks: `onPokemonClick`, `onBackClick`, `onEvolutionClick`, `onMovesClick`
- ViewModel state: `PokemonEvolutionUiState` with sealed interface pattern (Loading, Error, Success)

## Common Code Quality Issues
(None identified yet - first review)
