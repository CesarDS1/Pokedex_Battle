package com.cesar.pokedex.ui.screen.pokemonlist

import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Pokemon
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PokemonRepository = mockk {
        every { getFavoriteIds() } returns flowOf(emptySet())
    }

    private fun createViewModel(): PokemonListViewModel {
        return PokemonListViewModel(repository)
    }

    @Test
    fun `init loads pokemon list successfully`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass", "Poison")),
            Pokemon(id = 4, name = "Charmander", imageUrl = "https://example.com/4.png", types = listOf("Fire"))
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage)
            assertEquals(listOf("Generation I — Kanto"), state.pokemonByGeneration.keys.toList())
            assertEquals(pokemonList, state.pokemonByGeneration.values.flatten())
        }
    }

    @Test
    fun `init sets error state when repository throws`() = runTest {
        coEvery { repository.getPokemonList() } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Network error", state.errorMessage)
        }
    }

    @Test
    fun `retry after error transitions to success`() = runTest {
        coEvery { repository.getPokemonList() } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem().errorMessage != null)

            val pokemonList = listOf(
                Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass", "Poison"))
            )
            coEvery { repository.getPokemonList() } returns pokemonList

            viewModel.onEvent(PokemonListEvent.LoadPokemon)

            val success = awaitItem()
            assertNull(success.errorMessage)
            assertEquals(pokemonList, success.pokemonByGeneration.values.flatten())
        }
    }

    @Test
    fun `search filters pokemon by name`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass", "Poison")),
            Pokemon(id = 4, name = "Charmander", imageUrl = "https://example.com/4.png", types = listOf("Fire"))
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.onEvent(PokemonListEvent.Search("char"))

        viewModel.uiState.test {
            val state = awaitItem()
            val results = state.pokemonByGeneration.values.flatten()
            assertEquals(1, results.size)
            assertEquals("Charmander", results.first().name)
        }
    }

    @Test
    fun `pokemon types are preserved in list`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass", "Poison"))
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val pokemon = state.pokemonByGeneration.values.flatten().first()
            assertEquals(listOf("Grass", "Poison"), pokemon.types)
        }
    }

    @Test
    fun `search filters pokemon by id`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList()),
            Pokemon(id = 25, name = "Pikachu", imageUrl = "https://example.com/25.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.onEvent(PokemonListEvent.Search("25"))

        viewModel.uiState.test {
            val state = awaitItem()
            val results = state.pokemonByGeneration.values.flatten()
            assertEquals(1, results.size)
            assertEquals("Pikachu", results.first().name)
        }
    }

    @Test
    fun `refresh calls repository with forceRefresh`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList
        coEvery { repository.getPokemonList(forceRefresh = true) } returns pokemonList

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial success

            viewModel.onEvent(PokemonListEvent.RefreshPokemon)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repository.getPokemonList(forceRefresh = true) }
    }

    @Test
    fun `refresh sets isRefreshing to false when done`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList
        coEvery { repository.getPokemonList(forceRefresh = true) } returns pokemonList

        val viewModel = createViewModel()

        viewModel.onEvent(PokemonListEvent.RefreshPokemon)

        viewModel.uiState.test {
            assertEquals(false, awaitItem().isRefreshing)
        }
    }

    @Test
    fun `toggleGeneration adds and removes generation from collapsed set`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(emptySet<String>(), awaitItem().collapsedGenerations)

            viewModel.onEvent(PokemonListEvent.ToggleGeneration("Generation I — Kanto"))
            assertEquals(setOf("Generation I — Kanto"), awaitItem().collapsedGenerations)

            viewModel.onEvent(PokemonListEvent.ToggleGeneration("Generation I — Kanto"))
            assertEquals(emptySet<String>(), awaitItem().collapsedGenerations)
        }
    }

    @Test
    fun `pokemon from multiple generations are grouped correctly`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList()),
            Pokemon(id = 152, name = "Chikorita", imageUrl = "https://example.com/152.png", types = emptyList()),
            Pokemon(id = 387, name = "Turtwig", imageUrl = "https://example.com/387.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val grouped = state.pokemonByGeneration
            assertEquals(3, grouped.size)
            assertTrue(grouped.containsKey("Generation I — Kanto"))
            assertTrue(grouped.containsKey("Generation II — Johto"))
            assertTrue(grouped.containsKey("Generation IV — Sinnoh"))
        }
    }

    // region Favorites

    @Test
    fun `toggleShowFavoritesOnly setsShowFavoritesOnlyToTrue`() = runTest {
        coEvery { repository.getPokemonList() } returns emptyList()
        val viewModel = createViewModel()
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.showFavoritesOnly)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleShowFavoritesOnly togglesBackToFalse`() = runTest {
        coEvery { repository.getPokemonList() } returns emptyList()
        val viewModel = createViewModel()
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showFavoritesOnly)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showFavoritesOnly filtersToOnlyFavoritedPokemon`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass")),
            Pokemon(id = 4, name = "Charmander", imageUrl = "", types = listOf("Fire"))
        )
        every { repository.getFavoriteIds() } returns flowOf(setOf(1))
        coEvery { repository.getPokemonList() } returns pokemonList
        val viewModel = createViewModel()
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)

        viewModel.uiState.test {
            val state = awaitItem()
            val results = state.pokemonByGeneration.values.flatten()
            assertEquals(1, results.size)
            assertEquals("Bulbasaur", results[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showFavoritesOnly showsNoPokemonWhenNoFavorites`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass"))
        )
        coEvery { repository.getPokemonList() } returns pokemonList
        val viewModel = createViewModel()
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pokemonByGeneration.values.flatten().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favoriteIds areExposedInUiState`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 25, name = "Pikachu", imageUrl = "", types = emptyList())
        )
        every { repository.getFavoriteIds() } returns flowOf(setOf(25))
        coEvery { repository.getPokemonList() } returns pokemonList
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(setOf(25), state.favoriteIds)
            assertEquals(1, state.pokemonByGeneration.values.flatten().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favoriteIds reactToNewEmissions`() = runTest {
        val favoriteFlow = MutableStateFlow<Set<Int>>(emptySet())
        every { repository.getFavoriteIds() } returns favoriteFlow
        val pokemonList = listOf(
            Pokemon(id = 25, name = "Pikachu", imageUrl = "", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(emptySet<Int>(), initial.favoriteIds)

            favoriteFlow.value = setOf(25)

            val updated = awaitItem()
            assertEquals(setOf(25), updated.favoriteIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite callsRepositoryWithCorrectId`() = runTest {
        coJustRun { repository.toggleFavorite(any()) }
        coEvery { repository.getPokemonList() } returns emptyList()
        val viewModel = createViewModel()

        viewModel.onEvent(PokemonListEvent.ToggleFavorite(25))

        coVerify { repository.toggleFavorite(25) }
    }

    @Test
    fun `showFavoritesOnly combinedWithSearchFilter`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass")),
            Pokemon(id = 4, name = "Charmander", imageUrl = "", types = listOf("Fire")),
            Pokemon(id = 25, name = "Pikachu", imageUrl = "", types = listOf("Electric"))
        )
        every { repository.getFavoriteIds() } returns flowOf(setOf(1, 4))
        coEvery { repository.getPokemonList() } returns pokemonList
        val viewModel = createViewModel()
        viewModel.onEvent(PokemonListEvent.ToggleShowFavoritesOnly)
        viewModel.onEvent(PokemonListEvent.Search("bulb"))

        viewModel.uiState.test {
            val state = awaitItem()
            val results = state.pokemonByGeneration.values.flatten()
            assertEquals(1, results.size)
            assertEquals("Bulbasaur", results[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion
}
