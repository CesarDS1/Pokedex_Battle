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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TeamDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teamId = 1L
    private val savedStateHandle = SavedStateHandle(mapOf("teamId" to teamId))

    private val charizard = Pokemon(id = 6, name = "Charizard", imageUrl = "", types = listOf("Fire", "Flying"))
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
        coEvery { getPokemonDetail(charizard.id) } returns makeDetail(charizard)
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
    fun `remove member calls repository`() = runTest {
        coEvery { teamRepository.removeMember(teamId, charizard.id) } returns Unit

        val viewModel = createViewModel()
        viewModel.onEvent(TeamDetailEvent.RemoveMember(charizard.id))

        coVerify { teamRepository.removeMember(teamId, charizard.id) }
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
            assertNull(state.team)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `analysis shows weaknesses for fire flying team member`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val weaknesses = state.analysis?.weaknesses ?: emptyMap()
            // Fire/Flying is weak to Water, Electric, Rock
            assertTrue("Fire/Flying should be weak to water", (weaknesses["water"] ?: 0) >= 1)
            assertTrue("Fire/Flying should be weak to rock", (weaknesses["rock"] ?: 0) >= 1)
            assertTrue("Fire/Flying should be weak to electric", (weaknesses["electric"] ?: 0) >= 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `analysis offensive coverage includes types fire hits super effectively`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val coverage = state.analysis?.offensiveCoverage ?: emptySet()
            // Fire attacks are super effective against Grass, Ice, Bug, Steel
            assertTrue("fire coverage should include grass", "grass" in coverage)
            assertTrue("fire coverage should include ice", "ice" in coverage)
            assertTrue("fire coverage should include bug", "bug" in coverage)
            assertTrue("fire coverage should include steel", "steel" in coverage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `analysis detects coverage gap when two members share the same weakness`() = runTest {
        val magmar = Pokemon(id = 126, name = "Magmar", imageUrl = "", types = listOf("Fire"))
        val teamWithTwoFire = PokemonTeam(id = teamId, name = "Fire Team", members = listOf(charizard, magmar))
        every { teamRepository.getAllTeams() } returns flowOf(listOf(teamWithTwoFire))
        coEvery { pokemonRepository.getPokemonDetail(magmar.id) } returns makeDetail(magmar)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val gaps = state.analysis?.coverageGaps ?: emptyList()
            // Two Fire-type members → water is a coverage gap (2+ weak, 0 resist)
            assertTrue("water should be a coverage gap for two fire members", "water" in gaps)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `member details are loaded for each team member`() = runTest {
        val blastoise = Pokemon(id = 9, name = "Blastoise", imageUrl = "", types = listOf("Water"))
        val team = PokemonTeam(id = teamId, name = "Team", members = listOf(charizard, blastoise))
        every { teamRepository.getAllTeams() } returns flowOf(listOf(team))
        coEvery { pokemonRepository.getPokemonDetail(blastoise.id) } returns makeDetail(blastoise)

        createViewModel()

        coVerify { pokemonRepository.getPokemonDetail(charizard.id) }
        coVerify { pokemonRepository.getPokemonDetail(blastoise.id) }
    }

    @Test
    fun `analysis average stats are populated from loaded member details`() = runTest {
        val viewModel = createViewModel()

        // UnconfinedTestDispatcher runs init coroutines eagerly, so by the time we
        // subscribe, _memberDetails is already populated and the first emission has stats.
        viewModel.uiState.test {
            val state = awaitItem()
            val avgHp = state.analysis?.averageStats?.get("hp")
            assertNotNull("average hp stat should be present after details load", avgHp)
            assertEquals(78f, avgHp ?: 0f, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
