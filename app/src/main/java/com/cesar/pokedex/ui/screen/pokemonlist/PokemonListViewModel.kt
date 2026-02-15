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
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _collapsedGenerations = MutableStateFlow<Set<String>>(emptySet())
    val collapsedGenerations: StateFlow<Set<String>> = _collapsedGenerations

    private val _selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    val selectedTypes: StateFlow<Set<String>> = _selectedTypes

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleGeneration(generation: String) {
        _collapsedGenerations.value = _collapsedGenerations.value.let { current ->
            if (generation in current) current - generation else current + generation
        }
    }

    fun toggleTypeFilter(type: String) {
        _selectedTypes.value = _selectedTypes.value.let { current ->
            if (type in current) current - type else current + type
        }
    }

    fun clearTypeFilters() {
        _selectedTypes.value = emptySet()
    }

    val uiState: StateFlow<PokemonListUiState> = combine(
        _allPokemon,
        _searchQuery,
        _selectedTypes,
        favoriteIds,
        _showFavoritesOnly
    ) { loadState, query, selectedTypes, favorites, showFavOnly ->
        when (loadState) {
            is LoadState.Loading -> PokemonListUiState.Loading
            is LoadState.Error -> PokemonListUiState.Error(loadState.message)
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
                PokemonListUiState.Success(grouped)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PokemonListUiState.Loading)

    init {
        loadPokemon()
    }

    fun loadPokemon() {
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

    fun refreshPokemon() {
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    private sealed interface LoadState {
        data object Loading : LoadState
        data class Loaded(val pokemon: List<Pokemon>) : LoadState
        data class Error(val message: String) : LoadState
    }
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

sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Success(val pokemonByGeneration: Map<String, List<Pokemon>>) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}
