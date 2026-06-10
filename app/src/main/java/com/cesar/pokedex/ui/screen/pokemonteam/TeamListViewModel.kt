package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.repository.TeamRepository
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
class TeamListViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _showCreateDialog = MutableStateFlow(false)
    private val _pendingDeleteTeam = MutableStateFlow<PokemonTeam?>(null)

    val uiState = combine(
        teamRepository.getAllTeams(),
        _showCreateDialog,
        _pendingDeleteTeam
    ) { teams, showDialog, pendingDelete ->
        TeamListUiState(teams = teams, showCreateDialog = showDialog, pendingDeleteTeam = pendingDelete)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamListUiState())

    private val _events = Channel<TeamNavigationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEvent(event: TeamListEvent) {
        when (event) {
            TeamListEvent.ShowCreateDialog -> _showCreateDialog.value = true
            TeamListEvent.DismissCreateDialog -> _showCreateDialog.value = false
            is TeamListEvent.CreateTeam -> {
                _showCreateDialog.value = false
                viewModelScope.launch {
                    val id = teamRepository.createTeam(event.name)
                    _events.send(TeamNavigationEvent.NavigateToTeamDetail(id))
                }
            }
            is TeamListEvent.RequestDelete -> _pendingDeleteTeam.value = event.team
            TeamListEvent.ConfirmDelete -> {
                val team = _pendingDeleteTeam.value ?: return
                _pendingDeleteTeam.value = null
                viewModelScope.launch { teamRepository.deleteTeam(team.id) }
            }
            TeamListEvent.CancelDelete -> _pendingDeleteTeam.value = null
            is TeamListEvent.NavigateToTeam -> {
                viewModelScope.launch {
                    _events.send(TeamNavigationEvent.NavigateToTeamDetail(event.id))
                }
            }
        }
    }
}

data class TeamListUiState(
    val teams: List<PokemonTeam> = emptyList(),
    val showCreateDialog: Boolean = false,
    val pendingDeleteTeam: PokemonTeam? = null
)

sealed interface TeamListEvent {
    data object ShowCreateDialog : TeamListEvent
    data object DismissCreateDialog : TeamListEvent
    data class CreateTeam(val name: String) : TeamListEvent
    data class RequestDelete(val team: PokemonTeam) : TeamListEvent
    data object ConfirmDelete : TeamListEvent
    data object CancelDelete : TeamListEvent
    data class NavigateToTeam(val id: Long) : TeamListEvent
}

sealed interface TeamNavigationEvent {
    data class NavigateToTeamDetail(val teamId: Long) : TeamNavigationEvent
}
