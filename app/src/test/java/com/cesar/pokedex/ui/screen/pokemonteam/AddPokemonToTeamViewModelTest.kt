package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.domain.repository.TeamRepository
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

class AddPokemonToTeamViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teamId = 7L
    private val savedStateHandle = SavedStateHandle(mapOf("teamId" to teamId))

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass", "Poison"))
    private val charmander = Pokemon(id = 4, name = "Charmander", imageUrl = "", types = listOf("Fire"))
    private val squirtle = Pokemon(id = 7, name = "Squirtle", imageUrl = "", types = listOf("Water"))
    private val allPokemon = listOf(bulbasaur, charmander, squirtle)

    private val emptyTeam = PokemonTeam(id = teamId, name = "My Team", members = emptyList())
    private val teamsFlow = MutableStateFlow<List<PokemonTeam>>(listOf(emptyTeam))

    private val pokemonRepository: PokemonRepository = mockk {
        coEvery { getPokemonList() } returns allPokemon
    }

    private val teamRepository: TeamRepository = mockk {
        every { getAllTeams() } returns teamsFlow
        coJustRun { addMember(any(), any()) }
    }

    private fun createViewModel() =
        AddPokemonToTeamViewModel(savedStateHandle, pokemonRepository, teamRepository)

    @Test
    fun `initialState isLoadingThenShowsAllPokemon`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(3, state.pokemon.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initialState allTypesListIsPopulated`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(18, state.allTypes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filtersByNameCaseInsensitive`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.Search("char"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.pokemon.size)
            assertEquals("Charmander", state.pokemon[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filtersByPokemonId`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.Search("4"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.pokemon.size)
            assertEquals("Charmander", state.pokemon[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search blankQueryShowsAllPokemon`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.Search("char"))
        viewModel.onEvent(AddPokemonToTeamEvent.Search(""))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.pokemon.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTypeFilter addsTypeToSelectedSet`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Fire" in state.selectedTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTypeFilter removesTypeWhenAlreadySelected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Fire" !in state.selectedTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `typeFilter showsOnlyPokemonMatchingAnySelectedType`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.pokemon.size)
            assertEquals("Charmander", state.pokemon[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `typeFilter isOrLogic multipleTypesUnion`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Water"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.pokemon.size)
            assertTrue(state.pokemon.any { it.name == "Charmander" })
            assertTrue(state.pokemon.any { it.name == "Squirtle" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearTypeFilters removesAllTypeFilters`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))
        viewModel.onEvent(AddPokemonToTeamEvent.ClearTypeFilters)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.pokemon.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchAndTypeFilter areAppliedTogether`() = runTest {
        val viewModel = createViewModel()
        // "a" matches all three names; type "Fire" narrows to Charmander only
        viewModel.onEvent(AddPokemonToTeamEvent.Search("a"))
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleTypeFilter("Fire"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.pokemon.size)
            assertEquals("Charmander", state.pokemon[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `teamMemberExclusion excludesCurrentTeamMembers`() = runTest {
        val teamWithMember = PokemonTeam(id = teamId, name = "My Team", members = listOf(bulbasaur))
        every { teamRepository.getAllTeams() } returns flowOf(listOf(teamWithMember))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.pokemon.size)
            assertTrue(state.pokemon.none { it.id == bulbasaur.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `teamMemberExclusion excludesBeforeSearchFilter`() = runTest {
        val teamWithMember = PokemonTeam(id = teamId, name = "My Team", members = listOf(bulbasaur))
        every { teamRepository.getAllTeams() } returns flowOf(listOf(teamWithMember))
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.Search("bulba"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pokemon.none { it.id == bulbasaur.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addPokemon callsRepositoryAndEmitsPopBack`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onEvent(AddPokemonToTeamEvent.AddPokemon(charmander.id))
            val event = awaitItem()
            assertTrue(event is AddPokemonEvent.PopBack)
        }

        coVerify { teamRepository.addMember(teamId, charmander.id) }
    }

    @Test
    fun `loadingState isFalseAfterSuccessfulLoad`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadingState isFalseWhenRepositoryThrows`() = runTest {
        coEvery { pokemonRepository.getPokemonList() } throws RuntimeException("Network error")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleEnemyType addsTypeToSelectedEnemyTypes`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("water" in state.selectedEnemyTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleEnemyType removesTypeWhenAlreadySelected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("water" !in state.selectedEnemyTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearEnemyTypes removesAllEnemyTypeFilters`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("fire"))
        viewModel.onEvent(AddPokemonToTeamEvent.ClearEnemyTypes)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.selectedEnemyTypes.isEmpty())
            assertTrue(state.suggestions.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enemyTypes whenSelected populatesSuggestionsMapForAllPokemon`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))

        viewModel.uiState.test {
            val state = awaitItem()
            // scoreAll includes all pokemon, even those with negative or zero scores
            assertTrue(state.suggestions.isNotEmpty())
            // Bulbasaur (Grass/Poison): Grass hits water SE (+3) + resists water (+1) = +4
            assertTrue(state.suggestions.containsKey(bulbasaur.id))
            assertTrue((state.suggestions[bulbasaur.id] ?: 0) > 0)
            // Charmander (Fire): weak to Water (-2), should be in map with negative score
            assertTrue(state.suggestions.containsKey(charmander.id))
            assertTrue((state.suggestions[charmander.id] ?: 0) < 0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enemyTypes whenSelected sortsPokemonByScoreDescending`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))

        viewModel.uiState.test {
            val state = awaitItem()
            // Bulbasaur (+4) → Squirtle (+1) → Charmander (-2)
            val bulbasaurIndex = state.pokemon.indexOfFirst { it.id == bulbasaur.id }
            val squirtleIndex = state.pokemon.indexOfFirst { it.id == squirtle.id }
            val charmanderIndex = state.pokemon.indexOfFirst { it.id == charmander.id }
            assertTrue(bulbasaurIndex >= 0)
            assertTrue(bulbasaurIndex < squirtleIndex)
            assertTrue(squirtleIndex < charmanderIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enemyTypes whenCleared resetsSuggestionsAndOrder`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AddPokemonToTeamEvent.ToggleEnemyType("water"))
        viewModel.onEvent(AddPokemonToTeamEvent.ClearEnemyTypes)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.suggestions.isEmpty())
            assertTrue(state.selectedEnemyTypes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
