package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.model.TeamAnalysis
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.domain.repository.TeamRepository
import com.cesar.pokedex.domain.util.TeamAnalyzer
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

    val uiState: StateFlow<TeamDetailUiState> = combine(
        teamRepository.getAllTeams(),
        _memberDetails
    ) { teams, memberDetails ->
        val team = teams.firstOrNull { it.id == teamId }
            ?: return@combine TeamDetailUiState()

        val analysis = TeamAnalyzer.analyze(team.members, memberDetails)

        TeamDetailUiState(
            team = team,
            analysis = analysis
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamDetailUiState())

    init {
        observeTeamAndLoadDetails()
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
        }
    }
}

data class TeamDetailUiState(
    val team: PokemonTeam? = null,
    val analysis: TeamAnalysis? = null
)

sealed interface TeamDetailEvent {
    data class RemoveMember(val pokemonId: Int) : TeamDetailEvent
}
