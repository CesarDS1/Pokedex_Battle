package com.cesar.pokedex.ui.screen.pokemonmoves

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.Move
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonType
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

class PokemonMovesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PokemonRepository = mockk()

    private val testMoves = listOf(
        Move(name = "Thunder Shock", level = 1, type = "Electric"),
        Move(name = "Growl", level = 1, type = "Normal"),
        Move(name = "Thunderbolt", level = 26, type = "Electric")
    )

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
            Ability(name = "Static", isHidden = false)
        ),
        moves = testMoves
    )

    private fun createViewModel(pokemonId: Int = 25): PokemonMovesViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("pokemonId" to pokemonId))
        return PokemonMovesViewModel(savedStateHandle, repository)
    }

    @Test
    fun `init loads moves successfully`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonMovesUiState.Success)
            assertEquals(testMoves, (state as PokemonMovesUiState.Success).moves)
        }
    }

    @Test
    fun `init sets error state when repository throws`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonMovesUiState.Error)
            assertEquals("Network error", (state as PokemonMovesUiState.Error).message)
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
            assertTrue(awaitItem() is PokemonMovesUiState.Error)

            coEvery { repository.getPokemonDetail(25) } returns testDetail

            viewModel.onEvent(PokemonMovesEvent.LoadMoves)

            val success = awaitItem()
            assertTrue(success is PokemonMovesUiState.Success)
            assertEquals(testMoves, (success as PokemonMovesUiState.Success).moves)
        }
    }

    @Test
    fun `empty moves list is returned when detail has no moves`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail.copy(moves = emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonMovesUiState.Success)
            assertEquals(emptyList<Move>(), (state as PokemonMovesUiState.Success).moves)
        }
    }
}
