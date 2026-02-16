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
import kotlinx.coroutines.flow.combine
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

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    private val _isPlayingCry = MutableStateFlow(false)

    private val _isFavorite: StateFlow<Boolean> = repository.getFavoriteIds()
        .map { pokemonId in it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val uiState: StateFlow<PokemonDetailUiState> = combine(
        _loadState,
        _isFavorite,
        _isPlayingCry
    ) { loadState, isFavorite, isPlayingCry ->
        when (loadState) {
            is LoadState.Loading -> PokemonDetailUiState.Loading
            is LoadState.Error -> PokemonDetailUiState.Error(loadState.message)
            is LoadState.Success -> PokemonDetailUiState.Success(
                pokemon = loadState.pokemon,
                isFavorite = isFavorite,
                isPlayingCry = isPlayingCry
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PokemonDetailUiState.Loading)

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadPokemonDetail()
    }

    fun onEvent(event: PokemonDetailEvent) {
        when (event) {
            PokemonDetailEvent.LoadDetail -> loadPokemonDetail()
            PokemonDetailEvent.ToggleFavorite -> {
                viewModelScope.launch { repository.toggleFavorite(pokemonId) }
            }
            is PokemonDetailEvent.PlayCry -> playCry(event.url)
        }
    }

    private fun loadPokemonDetail() {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val detail = repository.getPokemonDetail(pokemonId)
                _loadState.value = LoadState.Success(detail)
            } catch (e: Exception) {
                _loadState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun playCry(url: String) {
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

    private sealed interface LoadState {
        data object Loading : LoadState
        data class Success(val pokemon: PokemonDetail) : LoadState
        data class Error(val message: String) : LoadState
    }
}

sealed interface PokemonDetailEvent {
    data object LoadDetail : PokemonDetailEvent
    data object ToggleFavorite : PokemonDetailEvent
    data class PlayCry(val url: String) : PokemonDetailEvent
}

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(
        val pokemon: PokemonDetail,
        val isFavorite: Boolean = false,
        val isPlayingCry: Boolean = false
    ) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}
