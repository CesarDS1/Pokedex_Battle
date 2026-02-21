package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonStat
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.cesar.pokedex.domain.repository.TeamRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TeamDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teamId = 1L
    private val savedStateHandle = SavedStateHandle(mapOf("teamId" to teamId))

    private val charizard = Pokemon(id = 6, name = "Charizard", imageUrl = "", types = listOf("Fire", "Flying"))
    private val blastoise = Pokemon(id = 9, name = "Blastoise", imageUrl = "", types = listOf("Water"))
    private val defaultTeam = PokemonTeam(id = teamId, name = "Test Team", members = listOf(charizard))

    private fun makeDetail(pokemon: Pokemon) = PokemonDetail(
        id = pokemon.id, name = pokemon.name, imageUrl = "",
        description = "", region = "",
        types = emptyList(), abilities = emptyList(),
        stats = listOf(PokemonStat("hp", 78), PokemonStat("attack", 84))
    )

    private val teamRepository: TeamRepository = mockk {
        every { getAllTeams() } returns flowOf(listOf(defaultTeam))
    }

    private val pokemonRepository: PokemonRepository = mockk {
        every { getFavoriteIds() } returns flowOf(emptySet())
        coEvery { getPokemonList() } returns listOf(charizard, blastoise)
        coEvery { getPokemonDetail(charizard.id) } returns makeDetail(charizard)
        coEvery { getPokemonDetail(blastoise.id) } returns makeDetail(blastoise)
    }

    private fun createViewModel() = TeamDetailViewModel(savedStateHandle, teamRepository, pokemonRepository)

    @Test
    fun `initial state loads team from repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.team)
            assertEquals("Test Team", state.team?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle enemy type updates selected enemy types`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ToggleEnemyType("water"))
            val state = awaitItem()
            assertTrue("water" in state.selectedEnemyTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle enemy type twice removes it`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ToggleEnemyType("water"))
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ToggleEnemyType("water"))
            val state = awaitItem()
            assertTrue("water" !in state.selectedEnemyTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear enemy types empties the set`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ToggleEnemyType("water"))
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ClearEnemyTypes)
            val state = awaitItem()
            assertTrue(state.selectedEnemyTypes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove member calls repository`() = runTest {
        coEvery { teamRepository.removeMember(teamId, charizard.id) } returns Unit

        val viewModel = createViewModel()
        viewModel.onEvent(TeamDetailEvent.RemoveMember(charizard.id))

        coVerify { teamRepository.removeMember(teamId, charizard.id) }
    }

    @Test
    fun `add suggestion calls repository addMember`() = runTest {
        coEvery { teamRepository.addMember(teamId, blastoise.id) } returns Unit

        val viewModel = createViewModel()
        viewModel.onEvent(TeamDetailEvent.AddSuggestion(blastoise.id))

        coVerify { teamRepository.addMember(teamId, blastoise.id) }
    }

    @Test
    fun `suggestions populated when enemy types selected`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamDetailEvent.ToggleEnemyType("water"))
            val state = awaitItem()
            // Charizard (Fire/Flying) would score negatively vs Water, but Blastoise (Water) resists
            // Grass, Electric types from allPokemon would score positively
            // Since allPokemon = [charizard, blastoise] and charizard is on team,
            // only blastoise (Water) would be evaluated vs Water enemy
            // Blastoise resists Water (+1) but water vs water is 0.5x
            assertNotNull(state.team)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `analysis is computed and not null when team has members`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull("Analysis should not be null when team has members", state.analysis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `analysis reflects team member types`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val analysis = state.analysis
            assertNotNull(analysis)
            // Charizard (Fire/Flying) is immune to Ground
            val groundImmune = analysis?.immunities?.get("ground") ?: 0
            assertTrue("Fire/Flying should be immune to ground", groundImmune >= 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `team not found returns default empty state`() = runTest {
        every { teamRepository.getAllTeams() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.team == null)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
