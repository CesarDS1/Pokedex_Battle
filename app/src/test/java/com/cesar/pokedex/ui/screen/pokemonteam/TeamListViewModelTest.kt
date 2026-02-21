package com.cesar.pokedex.ui.screen.pokemonteam

import app.cash.turbine.test
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.repository.TeamRepository
import com.cesar.pokedex.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TeamListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teamRepository: TeamRepository = mockk {
        every { getAllTeams() } returns flowOf(emptyList())
    }

    private fun createViewModel() = TeamListViewModel(teamRepository)

    @Test
    fun `initial state has empty teams and no dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.teams.isEmpty())
            assertFalse(state.showCreateDialog)
        }
    }

    @Test
    fun `teams from repository are mapped to ui state`() = runTest {
        val teams = listOf(
            PokemonTeam(id = 1L, name = "My Team", members = emptyList()),
            PokemonTeam(id = 2L, name = "Second Team", members = emptyList())
        )
        every { teamRepository.getAllTeams() } returns flowOf(teams)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.teams.size)
            assertEquals("My Team", state.teams[0].name)
        }
    }

    @Test
    fun `ShowCreateDialog event sets showCreateDialog to true`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertFalse(awaitItem().showCreateDialog)
            viewModel.onEvent(TeamListEvent.ShowCreateDialog)
            assertTrue(awaitItem().showCreateDialog)
        }
    }

    @Test
    fun `DismissCreateDialog event sets showCreateDialog to false`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamListEvent.ShowCreateDialog)
            assertTrue(awaitItem().showCreateDialog)
            viewModel.onEvent(TeamListEvent.DismissCreateDialog)
            assertFalse(awaitItem().showCreateDialog)
        }
    }

    @Test
    fun `CreateTeam event calls repository and emits navigation event`() = runTest {
        coEvery { teamRepository.createTeam("Test Team") } returns 42L

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onEvent(TeamListEvent.CreateTeam("Test Team"))
            val event = awaitItem()
            assertTrue(event is TeamNavigationEvent.NavigateToTeamDetail)
            assertEquals(42L, (event as TeamNavigationEvent.NavigateToTeamDetail).teamId)
        }

        coVerify { teamRepository.createTeam("Test Team") }
    }

    @Test
    fun `CreateTeam event dismisses dialog`() = runTest {
        coEvery { teamRepository.createTeam(any()) } returns 1L

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TeamListEvent.ShowCreateDialog)
            assertTrue(awaitItem().showCreateDialog)
            viewModel.onEvent(TeamListEvent.CreateTeam("Team"))
            assertFalse(awaitItem().showCreateDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteTeam event calls repository`() = runTest {
        coEvery { teamRepository.deleteTeam(1L) } returns Unit

        val viewModel = createViewModel()
        viewModel.onEvent(TeamListEvent.DeleteTeam(1L))

        coVerify { teamRepository.deleteTeam(1L) }
    }

    @Test
    fun `NavigateToTeam emits navigation event`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onEvent(TeamListEvent.NavigateToTeam(99L))
            val event = awaitItem()
            assertTrue(event is TeamNavigationEvent.NavigateToTeamDetail)
            assertEquals(99L, (event as TeamNavigationEvent.NavigateToTeamDetail).teamId)
        }
    }

    @Test
    fun `teams with members show member count in state`() = runTest {
        val members = listOf(
            Pokemon(id = 1, name = "Bulbasaur", imageUrl = "", types = listOf("Grass")),
            Pokemon(id = 4, name = "Charmander", imageUrl = "", types = listOf("Fire"))
        )
        val teams = listOf(PokemonTeam(id = 1L, name = "Team", members = members))
        every { teamRepository.getAllTeams() } returns flowOf(teams)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.teams.first().members.size)
        }
    }
}
