package com.cesar.pokedex.ui.screen.pokemoncompare

import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonCompareViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bulbasaurType =
        PokemonType(
            name = "Grass",
            apiName = "grass",
            weaknesses = emptyList(),
            resistances = emptyList(),
            strengths = emptyList(),
            ineffective = emptyList(),
        )
    private val charmanderType =
        PokemonType(
            name = "Fire",
            apiName = "fire",
            weaknesses = emptyList(),
            resistances = emptyList(),
            strengths = emptyList(),
            ineffective = emptyList(),
        )

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass"))
    private val charmander = Pokemon(id = 4, name = "Charmander", imageUrl = "", types = listOf("Fire"))
    private val squirtle = Pokemon(id = 7, name = "Squirtle", imageUrl = "", types = listOf("Water"))
    private val allPokemon = listOf(bulbasaur, charmander, squirtle)

    private val bulbasaurDetail =
        PokemonDetail(
            id = 1,
            name = "Bulbasaur",
            imageUrl = "",
            description = "",
            region = "Kanto",
            types = listOf(bulbasaurType),
            abilities = emptyList(),
        )
    private val charmanderDetail =
        PokemonDetail(
            id = 4,
            name = "Charmander",
            imageUrl = "",
            description = "",
            region = "Kanto",
            types = listOf(charmanderType),
            abilities = emptyList(),
        )

    private val pokemonRepository: PokemonRepository =
        mockk {
            coEvery { getPokemonList() } returns allPokemon
        }

    private fun createViewModel() = PokemonCompareViewModel(pokemonRepository)

    @Test
    fun `initialState rosterLoadsAndBothSlotsAreEmpty`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem()
                assertFalse(state.isRosterLoading)
                assertNull(state.slotA)
                assertNull(state.slotB)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `openSlotPicker setsActiveSheetSlot`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A))

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(ComparatorSlot.A, state.activeSheetSlot)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `closeSlotPicker clearsActiveSheetSlot`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A))
            viewModel.onEvent(PokemonCompareEvent.CloseSlotPicker)

            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.activeSheetSlot)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `searchInSheet filtersPickerResultsByNameOrId`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.SearchInSheet("char"))

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(1, state.pickerResults.size)
                assertEquals("Charmander", state.pickerResults[0].name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `pickPokemon forSlotA loadsDetailAndClosesSheet`() =
        runTest {
            coEvery { pokemonRepository.getPokemonDetail(bulbasaur.id) } returns bulbasaurDetail
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A))
            viewModel.onEvent(PokemonCompareEvent.PickPokemon(bulbasaur.id))

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(bulbasaurDetail, state.slotA)
                assertFalse(state.isLoadingSlotA)
                assertNull(state.activeSheetSlot)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `pickPokemon repositoryThrows setsErrorAndClearsLoading`() =
        runTest {
            coEvery { pokemonRepository.getPokemonDetail(bulbasaur.id) } throws RuntimeException("Network error")
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A))
            viewModel.onEvent(PokemonCompareEvent.PickPokemon(bulbasaur.id))

            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.slotA)
                assertFalse(state.isLoadingSlotA)
                assertNotNull(state.errorSlotA)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `pickerResults excludesTheOtherSlotsSelectedPokemon`() =
        runTest {
            coEvery { pokemonRepository.getPokemonDetail(bulbasaur.id) } returns bulbasaurDetail
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A))
            viewModel.onEvent(PokemonCompareEvent.PickPokemon(bulbasaur.id))
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.B))

            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.pickerResults.none { it.id == bulbasaur.id })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `clearSlot resetsThatSlotToNull`() =
        runTest {
            coEvery { pokemonRepository.getPokemonDetail(charmander.id) } returns charmanderDetail
            val viewModel = createViewModel()
            viewModel.onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.B))
            viewModel.onEvent(PokemonCompareEvent.PickPokemon(charmander.id))
            viewModel.onEvent(PokemonCompareEvent.ClearSlot(ComparatorSlot.B))

            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.slotB)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
