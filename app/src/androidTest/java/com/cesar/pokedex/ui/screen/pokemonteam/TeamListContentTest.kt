package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class TeamListContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", types = listOf("Grass"), imageUrl = "")
    private val charmander = Pokemon(id = 4, name = "Charmander", types = listOf("Fire"), imageUrl = "")

    private val sampleTeam = PokemonTeam(
        id = 1L,
        name = "Kanto Starters",
        members = listOf(bulbasaur, charmander)
    )

    @Test
    fun emptyState_showsNoTeamsMessage() {
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = emptyList()),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("No teams yet. Tap + to create one.").assertIsDisplayed()
    }

    @Test
    fun teamList_showsTeamName() {
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = listOf(sampleTeam)),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("Kanto Starters").assertIsDisplayed()
    }

    @Test
    fun teamList_showsMemberCount() {
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = listOf(sampleTeam)),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("2/6").assertIsDisplayed()
    }

    @Test
    fun createDialog_visibleWhenShowCreateDialogTrue() {
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = emptyList(), showCreateDialog = true),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("Create Team").assertIsDisplayed()
        composeRule.onNodeWithText("Create").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun createDialog_hiddenByDefault() {
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = emptyList(), showCreateDialog = false),
                    onEvent = {}
                )
            }
        }
        composeRule.onAllNodesWithText("Create Team").assertCountEquals(0)
    }

    @Test
    fun multipleTeams_allNamesDisplayed() {
        val secondTeam = PokemonTeam(id = 2L, name = "Elite Four", members = emptyList())
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = listOf(sampleTeam, secondTeam)),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("Kanto Starters").assertIsDisplayed()
        composeRule.onNodeWithText("Elite Four").assertIsDisplayed()
    }

    @Test
    fun emptyTeam_showsZeroMemberCount() {
        val emptyTeam = PokemonTeam(id = 3L, name = "Empty Team", members = emptyList())
        composeRule.setContent {
            PokedexTheme {
                TeamListContent(
                    uiState = TeamListUiState(teams = listOf(emptyTeam)),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("0/6").assertIsDisplayed()
    }
}
