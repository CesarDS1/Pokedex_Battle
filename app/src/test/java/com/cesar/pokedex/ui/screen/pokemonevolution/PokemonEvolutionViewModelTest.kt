package com.cesar.pokedex.ui.screen.pokemonevolution

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.EvolutionStage
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import com.cesar.pokedex.domain.model.PokemonVariety
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonEvolutionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PokemonRepository = mockk()

    private val testEvolutionInfo = PokemonEvolutionInfo(
        evolutions = listOf(
            EvolutionStage(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", trigger = "Base"),
            EvolutionStage(id = 2, name = "Ivysaur", imageUrl = "https://example.com/2.png", trigger = "Level 16"),
            EvolutionStage(id = 3, name = "Venusaur", imageUrl = "https://example.com/3.png", trigger = "Level 32")
        ),
        varieties = listOf(
            PokemonVariety(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", isDefault = true)
        )
    )

    private fun createViewModel(pokemonId: Int = 1): PokemonEvolutionViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to pokemonId))
        return PokemonEvolutionViewModel(savedStateHandle, repository)
    }

    @Test
    fun `init loads evolution info successfully`() = runTest {
        coEvery { repository.getEvolutionInfo(1) } returns testEvolutionInfo

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonEvolutionUiState.Success)
            val success = state as PokemonEvolutionUiState.Success
            assertEquals(testEvolutionInfo, success.info)
            assertEquals(1, success.currentPokemonId)
        }
    }

    @Test
    fun `init sets error state when repository throws`() = runTest {
        coEvery { repository.getEvolutionInfo(1) } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonEvolutionUiState.Error)
            assertEquals("Network error", (state as PokemonEvolutionUiState.Error).message)
        }
    }

    @Test
    fun `correct pokemon ID is forwarded to repository`() = runTest {
        coEvery { repository.getEvolutionInfo(25) } returns testEvolutionInfo

        val viewModel = createViewModel(pokemonId = 25)

        viewModel.uiState.test {
            awaitItem()
        }

        coVerify { repository.getEvolutionInfo(25) }
    }

    @Test
    fun `retry after error transitions to success`() = runTest {
        coEvery { repository.getEvolutionInfo(1) } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is PokemonEvolutionUiState.Error)

            coEvery { repository.getEvolutionInfo(1) } returns testEvolutionInfo

            viewModel.loadEvolutionInfo()

            val success = awaitItem()
            assertTrue(success is PokemonEvolutionUiState.Success)
            assertEquals(testEvolutionInfo, (success as PokemonEvolutionUiState.Success).info)
        }
    }

    @Test
    fun `success state includes current pokemon id`() = runTest {
        coEvery { repository.getEvolutionInfo(2) } returns testEvolutionInfo

        val viewModel = createViewModel(pokemonId = 2)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonEvolutionUiState.Success)
            assertEquals(2, (state as PokemonEvolutionUiState.Success).currentPokemonId)
        }
    }
}
