package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.model.TeamAnalysis
import com.cesar.pokedex.domain.model.TeamSuggestion
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.domain.repository.TeamRepository
import com.cesar.pokedex.domain.util.TeamAnalyzer
import com.cesar.pokedex.domain.util.TeamSuggestionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val teamRepository: TeamRepository,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val teamId: Long = checkNotNull(savedStateHandle["teamId"])

    private val _memberDetails = MutableStateFlow<List<PokemonDetail>>(emptyList())
    private val _allPokemon = MutableStateFlow<List<Pokemon>>(emptyList())
    private val _selectedEnemyTypes = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<TeamDetailUiState> = combine(
        teamRepository.getAllTeams(),
        _memberDetails,
        _allPokemon,
        _selectedEnemyTypes
    ) { teams, memberDetails, allPokemon, enemyTypes ->
        val team = teams.firstOrNull { it.id == teamId }
            ?: return@combine TeamDetailUiState()

        val analysis = TeamAnalyzer.analyze(team.members, memberDetails)
        val suggestions = TeamSuggestionEngine.suggest(
            allPokemon = allPokemon,
            enemyTypes = enemyTypes.toList(),
            teamMemberIds = team.members.map { it.id }
        )

        TeamDetailUiState(
            team = team,
            analysis = analysis,
            suggestions = suggestions,
            selectedEnemyTypes = enemyTypes,
            loadedMemberCount = memberDetails.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamDetailUiState())

    init {
        loadAllPokemon()
        observeTeamAndLoadDetails()
    }

    private fun loadAllPokemon() {
        viewModelScope.launch {
            try {
                _allPokemon.value = pokemonRepository.getPokemonList()
            } catch (_: Exception) { }
        }
    }

    private fun observeTeamAndLoadDetails() {
        viewModelScope.launch {
            teamRepository.getAllTeams().collect { teams ->
                val team = teams.firstOrNull { it.id == teamId } ?: return@collect
                loadMemberDetails(team)
            }
        }
    }

    private fun loadMemberDetails(team: PokemonTeam) {
        viewModelScope.launch {
            val details = team.members.mapNotNull { member ->
                try { pokemonRepository.getPokemonDetail(member.id) } catch (_: Exception) { null }
            }
            _memberDetails.value = details
        }
    }

    fun onEvent(event: TeamDetailEvent) {
        when (event) {
            is TeamDetailEvent.RemoveMember -> {
                viewModelScope.launch { teamRepository.removeMember(teamId, event.pokemonId) }
            }
            is TeamDetailEvent.ToggleEnemyType -> {
                _selectedEnemyTypes.value = _selectedEnemyTypes.value.let { current ->
                    if (event.type in current) current - event.type else current + event.type
                }
            }
            TeamDetailEvent.ClearEnemyTypes -> _selectedEnemyTypes.value = emptySet()
            is TeamDetailEvent.AddSuggestion -> {
                viewModelScope.launch { teamRepository.addMember(teamId, event.pokemonId) }
            }
        }
    }
}

data class TeamDetailUiState(
    val team: PokemonTeam? = null,
    val analysis: TeamAnalysis? = null,
    val suggestions: List<TeamSuggestion> = emptyList(),
    val selectedEnemyTypes: Set<String> = emptySet(),
    val loadedMemberCount: Int = 0
)

sealed interface TeamDetailEvent {
    data class RemoveMember(val pokemonId: Int) : TeamDetailEvent
    data class ToggleEnemyType(val type: String) : TeamDetailEvent
    data object ClearEnemyTypes : TeamDetailEvent
    data class AddSuggestion(val pokemonId: Int) : TeamDetailEvent
}
