package com.cesar.pokedex.ui.screen.pokemoncompare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ComparatorSlot { A, B }

@HiltViewModel
class PokemonCompareViewModel
    @Inject
    constructor(
        private val pokemonRepository: PokemonRepository,
    ) : ViewModel() {
        private val _allPokemon = MutableStateFlow<List<Pokemon>>(emptyList())
        private val _isRosterLoading = MutableStateFlow(true)
        private val _slotA = MutableStateFlow(SlotState())
        private val _slotB = MutableStateFlow(SlotState())
        private val _activeSheetSlot = MutableStateFlow<ComparatorSlot?>(null)
        private val _searchQuery = MutableStateFlow("")

        val uiState =
            combine(
                combine(_allPokemon, _searchQuery, _activeSheetSlot) { all, query, activeSlot ->
                    Triple(all, query, activeSlot)
                },
                _slotA,
                _slotB,
                _isRosterLoading,
            ) { triple, slotA, slotB, isRosterLoading ->
                val (allPokemon, query, activeSlot) = triple
                val excludedId =
                    when (activeSlot) {
                        ComparatorSlot.A -> slotB.detail?.id
                        ComparatorSlot.B -> slotA.detail?.id
                        null -> null
                    }
                val pickerResults =
                    allPokemon
                        .filter { it.id != excludedId }
                        .let { list ->
                            if (query.isBlank()) {
                                list
                            } else {
                                list.filter {
                                    it.name.contains(query, ignoreCase = true) ||
                                        it.id.toString().contains(query)
                                }
                            }
                        }

                PokemonCompareUiState(
                    slotA = slotA.detail,
                    slotB = slotB.detail,
                    isLoadingSlotA = slotA.isLoading,
                    isLoadingSlotB = slotB.isLoading,
                    errorSlotA = slotA.error,
                    errorSlotB = slotB.error,
                    activeSheetSlot = activeSlot,
                    isRosterLoading = isRosterLoading,
                    searchQuery = query,
                    pickerResults = pickerResults,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PokemonCompareUiState())

        init {
            viewModelScope.launch {
                try {
                    _allPokemon.value = pokemonRepository.getPokemonList()
                } catch (_: Exception) {
                }
                _isRosterLoading.value = false
            }
        }

        fun onEvent(event: PokemonCompareEvent) {
            when (event) {
                is PokemonCompareEvent.OpenSlotPicker -> {
                    _activeSheetSlot.value = event.slot
                    _searchQuery.value = ""
                }
                PokemonCompareEvent.CloseSlotPicker -> {
                    _activeSheetSlot.value = null
                    _searchQuery.value = ""
                }
                is PokemonCompareEvent.SearchInSheet -> _searchQuery.value = event.query
                is PokemonCompareEvent.PickPokemon -> pickPokemon(event.pokemonId)
                is PokemonCompareEvent.ClearSlot -> slotFlow(event.slot).value = SlotState()
            }
        }

        private fun pickPokemon(pokemonId: Int) {
            val slot = _activeSheetSlot.value ?: return
            val flow = slotFlow(slot)
            flow.value = flow.value.copy(isLoading = true, error = null)
            viewModelScope.launch {
                try {
                    val detail = pokemonRepository.getPokemonDetail(pokemonId)
                    flow.value = SlotState(detail = detail)
                    _activeSheetSlot.value = null
                    _searchQuery.value = ""
                } catch (e: Exception) {
                    flow.value = flow.value.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }

        private fun slotFlow(slot: ComparatorSlot): MutableStateFlow<SlotState> =
            when (slot) {
                ComparatorSlot.A -> _slotA
                ComparatorSlot.B -> _slotB
            }
    }

private data class SlotState(
    val detail: PokemonDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class PokemonCompareUiState(
    val slotA: PokemonDetail? = null,
    val slotB: PokemonDetail? = null,
    val isLoadingSlotA: Boolean = false,
    val isLoadingSlotB: Boolean = false,
    val errorSlotA: String? = null,
    val errorSlotB: String? = null,
    val activeSheetSlot: ComparatorSlot? = null,
    val isRosterLoading: Boolean = true,
    val searchQuery: String = "",
    val pickerResults: List<Pokemon> = emptyList(),
)

sealed interface PokemonCompareEvent {
    data class OpenSlotPicker(
        val slot: ComparatorSlot,
    ) : PokemonCompareEvent

    data object CloseSlotPicker : PokemonCompareEvent

    data class SearchInSheet(
        val query: String,
    ) : PokemonCompareEvent

    data class PickPokemon(
        val pokemonId: Int,
    ) : PokemonCompareEvent

    data class ClearSlot(
        val slot: ComparatorSlot,
    ) : PokemonCompareEvent
}
