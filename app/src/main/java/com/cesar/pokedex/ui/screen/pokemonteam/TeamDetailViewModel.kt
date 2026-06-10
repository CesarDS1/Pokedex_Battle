package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _showRenameDialog = MutableStateFlow(false)

    private val teamFlow = teamRepository.getAllTeams()
        .map { teams -> teams.firstOrNull { it.id == teamId } }
        .flatMapLatest { team ->
            if (team == null) return@flatMapLatest flowOf(TeamDetailUiState())
            flow {
                emit(TeamDetailUiState(team = team))
                val details = team.members.mapNotNull { member ->
                    try { pokemonRepository.getPokemonDetail(member.id) } catch (_: Exception) { null }
                }
                emit(TeamDetailUiState(
                    team = team,
                    analysis = TeamAnalyzer.analyze(team.members, details)
                ))
            }
        }

    val uiState: StateFlow<TeamDetailUiState> = combine(teamFlow, _showRenameDialog) { state, showDialog ->
        state.copy(showRenameDialog = showDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamDetailUiState())

    fun onEvent(event: TeamDetailEvent) {
        when (event) {
            is TeamDetailEvent.RemoveMember -> {
                viewModelScope.launch { teamRepository.removeMember(teamId, event.pokemonId) }
            }
            TeamDetailEvent.ShowRenameDialog -> _showRenameDialog.value = true
            TeamDetailEvent.DismissRenameDialog -> _showRenameDialog.value = false
            is TeamDetailEvent.ConfirmRename -> {
                _showRenameDialog.value = false
                viewModelScope.launch { teamRepository.renameTeam(teamId, event.name) }
            }
            is TeamDetailEvent.SwapMembers -> {
                val members = uiState.value.team?.members ?: return
                val list = members.toMutableList()
                if (event.fromIndex !in list.indices || event.toIndex !in list.indices) return
                val temp = list[event.fromIndex]
                list[event.fromIndex] = list[event.toIndex]
                list[event.toIndex] = temp
                viewModelScope.launch { teamRepository.reorderMembers(teamId, list.map { it.id }) }
            }
        }
    }
}

data class TeamDetailUiState(
    val team: PokemonTeam? = null,
    val analysis: TeamAnalysis? = null,
    val showRenameDialog: Boolean = false
)

sealed interface TeamDetailEvent {
    data class RemoveMember(val pokemonId: Int) : TeamDetailEvent
    data object ShowRenameDialog : TeamDetailEvent
    data object DismissRenameDialog : TeamDetailEvent
    data class ConfirmRename(val name: String) : TeamDetailEvent
    data class SwapMembers(val fromIndex: Int, val toIndex: Int) : TeamDetailEvent
}
