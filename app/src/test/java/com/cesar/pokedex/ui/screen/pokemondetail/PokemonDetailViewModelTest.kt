package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PokemonRepository = mockk {
        every { getFavoriteIds() } returns flowOf(emptySet())
    }

    private val testDetail = PokemonDetail(
        id = 25,
        name = "Pikachu",
        imageUrl = "https://example.com/25.png",
        description = "An electric mouse.",
        region = "Kanto",
        types = listOf(
            PokemonType(
                name = "Electric",
                weaknesses = listOf("Ground"),
                resistances = listOf("Flying", "Steel", "Electric"),
                strengths = listOf("Water", "Flying"),
                ineffective = listOf("Ground")
            )
        ),
        abilities = listOf(
            Ability(name = "Static", isHidden = false),
            Ability(name = "Lightning Rod", isHidden = true)
        ),
        cryUrl = "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/25.ogg"
    )

    private fun createViewModel(pokemonId: Int = 25): PokemonDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to pokemonId))
        return PokemonDetailViewModel(savedStateHandle, repository)
    }

    @Test
    fun `init loads pokemon detail successfully`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Success)
            assertEquals(testDetail, (state as PokemonDetailUiState.Success).pokemon)
        }
    }

    @Test
    fun `init sets error state when repository throws`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("Not found")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Error)
            assertEquals("Not found", (state as PokemonDetailUiState.Error).message)
        }
    }

    @Test
    fun `correct pokemon ID is forwarded to repository`() = runTest {
        coEvery { repository.getPokemonDetail(42) } returns testDetail.copy(id = 42)

        val viewModel = createViewModel(pokemonId = 42)

        viewModel.uiState.test {
            awaitItem()
        }

        coVerify { repository.getPokemonDetail(42) }
    }

    @Test
    fun `retry after error transitions to success`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is PokemonDetailUiState.Error)

            coEvery { repository.getPokemonDetail(25) } returns testDetail

            viewModel.loadPokemonDetail()

            val success = awaitItem()
            assertTrue(success is PokemonDetailUiState.Success)
            assertEquals(testDetail, (success as PokemonDetailUiState.Success).pokemon)
        }
    }

    @Test
    fun `isPlayingCry is initially false`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail

        val viewModel = createViewModel()

        viewModel.isPlayingCry.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `success state includes cryUrl`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Success)
            assertEquals(
                "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/25.ogg",
                (state as PokemonDetailUiState.Success).pokemon.cryUrl
            )
        }
    }
}
