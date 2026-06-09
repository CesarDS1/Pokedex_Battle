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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    val uiState: StateFlow<TeamDetailUiState> = teamRepository.getAllTeams()
        .map { teams -> teams.firstOrNull { it.id == teamId } }
        .flatMapLatest { team ->
            if (team == null) return@flatMapLatest flowOf(TeamDetailUiState())
            flow {
                val details = team.members.mapNotNull { member ->
                    try { pokemonRepository.getPokemonDetail(member.id) } catch (_: Exception) { null }
                }
                emit(TeamDetailUiState(
                    team = team,
                    analysis = TeamAnalyzer.analyze(team.members, details)
                ))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamDetailUiState())

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
