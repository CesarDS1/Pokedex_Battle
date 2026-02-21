# Android Expert Developer Memory

## Project Structure
See CLAUDE.md for full layout. Key additions beyond CLAUDE.md:
- `ui/screen/pokemonteam/` — TeamDetailViewModel, TeamDetailUiState, TeamDetailEvent (all in TeamDetailViewModel.kt)
- `domain/util/TeamAnalyzer.kt`, `TeamSuggestionEngine.kt` — pure logic utilities

## Test Dispatcher
`MainDispatcherRule` uses `UnconfinedTestDispatcher`. This means coroutines launched in `init {}` complete eagerly before `awaitItem()` is called. The first `awaitItem()` therefore returns the *settled* final state, not any intermediate loading state.

## Repository Cache-Bypass Pattern
`PokemonRepositoryImpl.getPokemonList()` checks the cache by inspecting `cached.firstOrNull()?.types?.firstOrNull()`. It re-fetches if the first type is null (empty list) or is not in the `englishTypeNames` set (localized/stale cache). Tests for this pattern stub `dao.getAllPokemon()` with the stale entity, then verify `dao.deleteAllPokemon()` and `api.getPokemonList(limit=1)` were called.

## Test Stub Conventions (PokemonRepositoryImplTest)
- `stubTypeListEmpty()` — stubs `api.getTypeList(any())` to return empty list; call this whenever testing `fetchAndCachePokemonList()` without caring about type data.
- `stubDetailAndSpecies(...)` — comprehensive helper for detail/species/type/ability API mocks.
- `stubEvolutionSpecies(id, name)` — stubs a minimal species response for evolution chain helpers.
- `@Before setUp()` stubs: locale="en", `dao.getAllPokemon()`=emptyList, dao insert/delete as justRun.

## ViewModel StateFlow Testing Pattern
```kotlin
viewModel.uiState.test {
    val state = awaitItem()   // first settled state with UnconfinedTestDispatcher
    // assert on state
    cancelAndIgnoreRemainingEvents()
}
```
To override a class-level `coEvery` stub, re-declare it inside the test before creating the ViewModel.

## Files Reference
- Production repo: `app/src/main/java/com/cesar/pokedex/data/repository/PokemonRepositoryImpl.kt`
- Repo test: `app/src/test/java/com/cesar/pokedex/data/repository/PokemonRepositoryImplTest.kt`
- TeamDetail VM: `app/src/main/java/com/cesar/pokedex/ui/screen/pokemonteam/TeamDetailViewModel.kt`
- TeamDetail VM test: `app/src/test/java/com/cesar/pokedex/ui/screen/pokemonteam/TeamDetailViewModelTest.kt`
- Test rule: `app/src/test/java/com/cesar/pokedex/util/MainDispatcherRule.kt`
