package com.cesar.pokedex.ui.screen.pokemonevolution

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import com.cesar.pokedex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonEvolutionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow<PokemonEvolutionUiState>(PokemonEvolutionUiState.Loading)
    val uiState: StateFlow<PokemonEvolutionUiState> = _uiState

    init {
        loadEvolutionInfo()
    }

    fun onEvent(event: PokemonEvolutionEvent) {
        when (event) {
            PokemonEvolutionEvent.LoadEvolution -> loadEvolutionInfo()
        }
    }

    private fun loadEvolutionInfo() {
        viewModelScope.launch {
            _uiState.value = PokemonEvolutionUiState.Loading
            try {
                val info = repository.getEvolutionInfo(pokemonId)
                _uiState.value = PokemonEvolutionUiState.Success(info, pokemonId)
            } catch (e: Exception) {
                _uiState.value = PokemonEvolutionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface PokemonEvolutionEvent {
    data object LoadEvolution : PokemonEvolutionEvent
}

sealed interface PokemonEvolutionUiState {
    data object Loading : PokemonEvolutionUiState
    data class Success(val info: PokemonEvolutionInfo, val currentPokemonId: Int) : PokemonEvolutionUiState
    data class Error(val message: String) : PokemonEvolutionUiState
}
