package com.cesar.pokedex.ui.screen.pokemonlist

import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Pokemon
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

class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: PokemonRepository = mockk()

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
            assertTrue(state is PokemonListUiState.Success)
            val grouped = (state as PokemonListUiState.Success).pokemonByGeneration
            assertEquals(listOf("Generation I — Kanto"), grouped.keys.toList())
            assertEquals(pokemonList, grouped.values.flatten())
        }
    }

    @Test
    fun `init sets error state when repository throws`() = runTest {
        coEvery { repository.getPokemonList() } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Error)
            assertEquals("Network error", (state as PokemonListUiState.Error).message)
        }
    }

    @Test
    fun `retry after error transitions to success`() = runTest {
        coEvery { repository.getPokemonList() } throws RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is PokemonListUiState.Error)

            val pokemonList = listOf(
                Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass", "Poison"))
            )
            coEvery { repository.getPokemonList() } returns pokemonList

            viewModel.loadPokemon()

            val success = awaitItem()
            assertTrue(success is PokemonListUiState.Success)
            val grouped = (success as PokemonListUiState.Success).pokemonByGeneration
            assertEquals(pokemonList, grouped.values.flatten())
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

        viewModel.onSearchQueryChanged("char")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Success)
            val results = (state as PokemonListUiState.Success).pokemonByGeneration.values.flatten()
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
            assertTrue(state is PokemonListUiState.Success)
            val pokemon = (state as PokemonListUiState.Success).pokemonByGeneration.values.flatten().first()
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

        viewModel.onSearchQueryChanged("25")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Success)
            val results = (state as PokemonListUiState.Success).pokemonByGeneration.values.flatten()
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

            viewModel.refreshPokemon()

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

        viewModel.refreshPokemon()

        viewModel.isRefreshing.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `toggleGeneration adds and removes generation from collapsed set`() = runTest {
        val pokemonList = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList())
        )
        coEvery { repository.getPokemonList() } returns pokemonList

        val viewModel = createViewModel()

        viewModel.collapsedGenerations.test {
            assertEquals(emptySet<String>(), awaitItem())

            viewModel.toggleGeneration("Generation I — Kanto")
            assertEquals(setOf("Generation I — Kanto"), awaitItem())

            viewModel.toggleGeneration("Generation I — Kanto")
            assertEquals(emptySet<String>(), awaitItem())
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
            assertTrue(state is PokemonListUiState.Success)
            val grouped = (state as PokemonListUiState.Success).pokemonByGeneration
            assertEquals(3, grouped.size)
            assertTrue(grouped.containsKey("Generation I — Kanto"))
            assertTrue(grouped.containsKey("Generation II — Johto"))
            assertTrue(grouped.containsKey("Generation IV — Sinnoh"))
        }
    }
}
