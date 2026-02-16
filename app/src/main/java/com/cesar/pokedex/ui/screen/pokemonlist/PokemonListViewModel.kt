package com.cesar.pokedex.ui.screen.pokemonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _allPokemon = MutableStateFlow<LoadState>(LoadState.Loading)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    private val _collapsedGenerations = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    private val _showFavoritesOnly = MutableStateFlow(false)

    private val _favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val uiState: StateFlow<PokemonListUiState> = combine(
        _allPokemon,
        _searchQuery,
        _selectedTypes,
        _favoriteIds,
        _showFavoritesOnly
    ) { loadState, query, selectedTypes, favorites, showFavOnly ->
        when (loadState) {
            is LoadState.Loading -> PokemonListUiState(isLoading = true)
            is LoadState.Error -> PokemonListUiState(errorMessage = loadState.message)
            is LoadState.Loaded -> {
                val searchFiltered = if (query.isBlank()) {
                    loadState.pokemon
                } else {
                    loadState.pokemon.filter { pokemon ->
                        pokemon.name.contains(query, ignoreCase = true) ||
                                pokemon.id.toString().contains(query)
                    }
                }
                val typeFiltered = if (selectedTypes.isEmpty()) {
                    searchFiltered
                } else {
                    searchFiltered.filter { pokemon ->
                        pokemon.types.any { it.lowercase() in selectedTypes.map { s -> s.lowercase() } }
                    }
                }
                val filtered = if (showFavOnly) {
                    typeFiltered.filter { it.id in favorites }
                } else {
                    typeFiltered
                }
                val grouped = filtered
                    .sortedBy { it.id }
                    .groupBy { generationOf(it.id) }
                PokemonListUiState(
                    pokemonByGeneration = grouped,
                    searchQuery = query,
                    selectedTypes = selectedTypes,
                    favoriteIds = favorites,
                    showFavoritesOnly = showFavOnly
                )
            }
        }
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_collapsedGenerations) { state, collapsed ->
        state.copy(collapsedGenerations = collapsed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PokemonListUiState(isLoading = true))

    init {
        loadPokemon()
    }

    fun onEvent(event: PokemonListEvent) {
        when (event) {
            is PokemonListEvent.Search -> _searchQuery.value = event.query
            is PokemonListEvent.ToggleGeneration -> {
                _collapsedGenerations.value = _collapsedGenerations.value.let { current ->
                    if (event.generation in current) current - event.generation else current + event.generation
                }
            }
            is PokemonListEvent.ToggleTypeFilter -> {
                _selectedTypes.value = _selectedTypes.value.let { current ->
                    if (event.type in current) current - event.type else current + event.type
                }
            }
            PokemonListEvent.ClearTypeFilters -> _selectedTypes.value = emptySet()
            is PokemonListEvent.ToggleFavorite -> {
                viewModelScope.launch { repository.toggleFavorite(event.id) }
            }
            PokemonListEvent.ToggleShowFavoritesOnly -> {
                _showFavoritesOnly.value = !_showFavoritesOnly.value
            }
            PokemonListEvent.LoadPokemon -> loadPokemon()
            PokemonListEvent.RefreshPokemon -> refreshPokemon()
        }
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            _allPokemon.value = LoadState.Loading
            try {
                val pokemon = repository.getPokemonList()
                _allPokemon.value = LoadState.Loaded(pokemon)
            } catch (e: Exception) {
                _allPokemon.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun refreshPokemon() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val pokemon = repository.getPokemonList(forceRefresh = true)
                _allPokemon.value = LoadState.Loaded(pokemon)
            } catch (e: Exception) {
                _allPokemon.value = LoadState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private sealed interface LoadState {
        data object Loading : LoadState
        data class Loaded(val pokemon: List<Pokemon>) : LoadState
        data class Error(val message: String) : LoadState
    }
}

sealed interface PokemonListEvent {
    data class Search(val query: String) : PokemonListEvent
    data class ToggleGeneration(val generation: String) : PokemonListEvent
    data class ToggleTypeFilter(val type: String) : PokemonListEvent
    data object ClearTypeFilters : PokemonListEvent
    data class ToggleFavorite(val id: Int) : PokemonListEvent
    data object ToggleShowFavoritesOnly : PokemonListEvent
    data object LoadPokemon : PokemonListEvent
    data object RefreshPokemon : PokemonListEvent
}

val ALL_POKEMON_TYPES = listOf(
    "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
    "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
    "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
)

fun generationOf(id: Int): String = when {
    id <= 151 -> "Generation I — Kanto"
    id <= 251 -> "Generation II — Johto"
    id <= 386 -> "Generation III — Hoenn"
    id <= 493 -> "Generation IV — Sinnoh"
    id <= 649 -> "Generation V — Unova"
    id <= 721 -> "Generation VI — Kalos"
    id <= 809 -> "Generation VII — Alola"
    id <= 905 -> "Generation VIII — Galar"
    id <= 1025 -> "Generation IX — Paldea"
    else -> "Other"
}

data class PokemonListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pokemonByGeneration: Map<String, List<Pokemon>> = emptyMap(),
    val searchQuery: String = "",
    val collapsedGenerations: Set<String> = emptySet(),
    val isRefreshing: Boolean = false,
    val selectedTypes: Set<String> = emptySet(),
    val favoriteIds: Set<Int> = emptySet(),
    val showFavoritesOnly: Boolean = false
)
