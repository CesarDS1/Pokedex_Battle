package com.cesar.pokedex.ui.screen.pokemonmoves

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.Move
import com.cesar.pokedex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonMovesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow<PokemonMovesUiState>(PokemonMovesUiState.Loading)
    val uiState: StateFlow<PokemonMovesUiState> = _uiState

    init {
        loadMoves()
    }

    fun onEvent(event: PokemonMovesEvent) {
        when (event) {
            PokemonMovesEvent.LoadMoves -> loadMoves()
        }
    }

    private fun loadMoves() {
        viewModelScope.launch {
            _uiState.value = PokemonMovesUiState.Loading
            try {
                val detail = repository.getPokemonDetail(pokemonId)
                _uiState.value = PokemonMovesUiState.Success(detail.moves)
            } catch (e: Exception) {
                _uiState.value = PokemonMovesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface PokemonMovesEvent {
    data object LoadMoves : PokemonMovesEvent
}

sealed interface PokemonMovesUiState {
    data object Loading : PokemonMovesUiState
    data class Success(val moves: List<Move>) : PokemonMovesUiState
    data class Error(val message: String) : PokemonMovesUiState
}
