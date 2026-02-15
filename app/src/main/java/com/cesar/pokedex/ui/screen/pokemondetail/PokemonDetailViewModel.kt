package com.cesar.pokedex.ui.screen.pokemondetail

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState

    val isFavorite: StateFlow<Boolean> = repository.getFavoriteIds()
        .map { pokemonId in it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var mediaPlayer: MediaPlayer? = null
    private val _isPlayingCry = MutableStateFlow(false)
    val isPlayingCry: StateFlow<Boolean> = _isPlayingCry

    init {
        loadPokemonDetail()
    }

    fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            try {
                val detail = repository.getPokemonDetail(pokemonId)
                _uiState.value = PokemonDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = PokemonDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(pokemonId)
        }
    }

    fun playCry(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener {
                _isPlayingCry.value = true
                start()
            }
            setOnCompletionListener {
                _isPlayingCry.value = false
            }
            setOnErrorListener { _, _, _ ->
                _isPlayingCry.value = false
                true
            }
            prepareAsync()
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetail) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}
