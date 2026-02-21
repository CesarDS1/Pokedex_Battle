package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.domain.repository.TeamRepository
import com.cesar.pokedex.ui.screen.pokemonlist.ALL_POKEMON_TYPES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPokemonToTeamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val teamId: Long = checkNotNull(savedStateHandle["teamId"])

    private val _allPokemon = MutableStateFlow<List<Pokemon>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(true)

    val uiState = combine(
        _allPokemon,
        _searchQuery,
        _selectedTypes,
        _isLoading,
        teamRepository.getAllTeams()
    ) { allPokemon, query, selectedTypes, isLoading, teams ->
        val teamMemberIds = teams.firstOrNull { it.id == teamId }?.members?.map { it.id }?.toSet() ?: emptySet()

        val filtered = allPokemon
            .filter { it.id !in teamMemberIds }
            .let { list ->
                if (query.isBlank()) list
                else list.filter { it.name.contains(query, ignoreCase = true) || it.id.toString().contains(query) }
            }
            .let { list ->
                if (selectedTypes.isEmpty()) list
                else list.filter { pokemon -> pokemon.types.any { it.lowercase() in selectedTypes.map { s -> s.lowercase() } } }
            }

        AddPokemonUiState(
            pokemon = filtered,
            searchQuery = query,
            selectedTypes = selectedTypes,
            isLoading = isLoading,
            teamMemberIds = teamMemberIds,
            allTypes = ALL_POKEMON_TYPES
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddPokemonUiState())

    private val _events = Channel<AddPokemonEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                _allPokemon.value = pokemonRepository.getPokemonList()
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    fun onEvent(event: AddPokemonToTeamEvent) {
        when (event) {
            is AddPokemonToTeamEvent.Search -> _searchQuery.value = event.query
            is AddPokemonToTeamEvent.ToggleTypeFilter -> {
                _selectedTypes.value = _selectedTypes.value.let { current ->
                    if (event.type in current) current - event.type else current + event.type
                }
            }
            AddPokemonToTeamEvent.ClearTypeFilters -> _selectedTypes.value = emptySet()
            is AddPokemonToTeamEvent.AddPokemon -> {
                viewModelScope.launch {
                    teamRepository.addMember(teamId, event.pokemonId)
                    _events.send(AddPokemonEvent.PopBack)
                }
            }
        }
    }
}

data class AddPokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val searchQuery: String = "",
    val selectedTypes: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val teamMemberIds: Set<Int> = emptySet(),
    val allTypes: List<String> = emptyList()
)

sealed interface AddPokemonToTeamEvent {
    data class Search(val query: String) : AddPokemonToTeamEvent
    data class ToggleTypeFilter(val type: String) : AddPokemonToTeamEvent
    data object ClearTypeFilters : AddPokemonToTeamEvent
    data class AddPokemon(val pokemonId: Int) : AddPokemonToTeamEvent
}

sealed interface AddPokemonEvent {
    data object PopBack : AddPokemonEvent
}
