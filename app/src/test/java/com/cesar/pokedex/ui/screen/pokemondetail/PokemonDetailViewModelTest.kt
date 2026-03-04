package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

            viewModel.onEvent(PokemonDetailEvent.LoadDetail)

            val success = awaitItem()
            assertTrue(success is PokemonDetailUiState.Success)
            assertEquals(testDetail, (success as PokemonDetailUiState.Success).pokemon)
        }
    }

    @Test
    fun `isPlayingCry is initially false`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Success)
            assertEquals(false, (state as PokemonDetailUiState.Success).isPlayingCry)
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

    // region Favorites

    @Test
    fun `isFavorite isTrueWhenPokemonIdIsInFavoriteSet`() = runTest {
        every { repository.getFavoriteIds() } returns flowOf(setOf(25))
        coEvery { repository.getPokemonDetail(25) } returns testDetail
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Success)
            assertTrue((state as PokemonDetailUiState.Success).isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavorite isFalseWhenPokemonIdIsNotInFavoriteSet`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns testDetail
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonDetailUiState.Success)
            assertFalse((state as PokemonDetailUiState.Success).isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavorite reactsToNewEmissionFromFavoriteIds`() = runTest {
        val favoriteFlow = MutableStateFlow<Set<Int>>(emptySet())
        every { repository.getFavoriteIds() } returns favoriteFlow
        coEvery { repository.getPokemonDetail(25) } returns testDetail
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial is PokemonDetailUiState.Success)
            assertFalse((initial as PokemonDetailUiState.Success).isFavorite)

            favoriteFlow.value = setOf(25)

            val updated = awaitItem()
            assertTrue(updated is PokemonDetailUiState.Success)
            assertTrue((updated as PokemonDetailUiState.Success).isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite callsRepositoryToggleFavoriteWithPokemonId`() = runTest {
        coJustRun { repository.toggleFavorite(any()) }
        coEvery { repository.getPokemonDetail(25) } returns testDetail
        val viewModel = createViewModel()

        viewModel.onEvent(PokemonDetailEvent.ToggleFavorite)

        coVerify { repository.toggleFavorite(25) }
    }

    @Test
    fun `toggleFavorite doesNotChangeLoadStateDirectly`() = runTest {
        coJustRun { repository.toggleFavorite(any()) }
        coEvery { repository.getPokemonDetail(25) } returns testDetail
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val before = awaitItem()
            assertTrue(before is PokemonDetailUiState.Success)

            viewModel.onEvent(PokemonDetailEvent.ToggleFavorite)

            // No new emission for Success type (isFavorite unchanged here since mock returns same flow)
            // The load state remains Success after the toggle
            cancelAndIgnoreRemainingEvents()
        }

        val finalState = viewModel.uiState.value
        assertTrue(finalState is PokemonDetailUiState.Success)
    }

    // endregion
}
