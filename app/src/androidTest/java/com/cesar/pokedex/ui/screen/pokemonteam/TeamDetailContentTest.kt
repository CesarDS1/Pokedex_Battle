package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.model.TeamAnalysis
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class TeamDetailContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val charizard = Pokemon(id = 6, name = "Charizard", types = listOf("Fire", "Flying"), imageUrl = "")
    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", types = listOf("Grass", "Poison"), imageUrl = "")

    private val teamWithMembers = PokemonTeam(
        id = 1L,
        name = "My Team",
        members = listOf(charizard)
    )

    private val emptyTeam = PokemonTeam(id = 2L, name = "Empty Team", members = emptyList())

    private val sampleAnalysis = TeamAnalysis(
        weaknesses = mapOf("water" to 1, "rock" to 1),
        resistances = mapOf("fire" to 1, "grass" to 1),
        immunities = mapOf("ground" to 1),
        offensiveCoverage = setOf("grass", "ice", "bug", "steel"),
        averageStats = mapOf("hp" to 78f, "attack" to 84f),
        totalStats = mapOf("hp" to 78, "attack" to 84),
        coverageGaps = emptyList()
    )

    @Test
    fun tabsAreDisplayed() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = teamWithMembers, analysis = sampleAnalysis),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Roster").assertIsDisplayed()
        composeRule.onNodeWithText("Analysis").assertIsDisplayed()
    }

    @Test
    fun rosterTab_showsMemberName() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = teamWithMembers, analysis = sampleAnalysis),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Charizard").assertIsDisplayed()
    }

    @Test
    fun rosterTab_emptyTeam_showsAddSlots() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = emptyTeam, analysis = null),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        // Empty team shows 6 "Add" slots
        composeRule.onAllNodesWithText("Add").assertCountEquals(6)
    }

    @Test
    fun rosterTab_partialTeam_showsCorrectSlotCounts() {
        val teamWithOne = PokemonTeam(id = 3L, name = "Team", members = listOf(charizard))
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = teamWithOne, analysis = sampleAnalysis),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Charizard").assertIsDisplayed()
        // 5 empty slots remaining
        composeRule.onAllNodesWithText("Add").assertCountEquals(5)
    }

    @Test
    fun analysisTab_nullAnalysis_showsHintMessage() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = emptyTeam, analysis = null),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        // Tap the Analysis tab
        composeRule.onNodeWithText("Analysis").performClick()
        composeRule.onNodeWithText("Add Pokemon to see team analysis.").assertIsDisplayed()
    }

    @Test
    fun analysisTab_showsSectionTitles() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = teamWithMembers, analysis = sampleAnalysis),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Analysis").performClick()
        composeRule.onNodeWithText("Defensive Weaknesses").assertIsDisplayed()
        composeRule.onNodeWithText("Defensive Resistances").assertIsDisplayed()
        composeRule.onNodeWithText("Offensive Coverage").assertIsDisplayed()
    }

    @Test
    fun topBar_showsTeamName() {
        composeRule.setContent {
            PokedexTheme {
                TeamDetailScreenContent(
                    uiState = TeamDetailUiState(team = teamWithMembers, analysis = sampleAnalysis),
                    onEvent = {},
                    onBackClick = {},
                    onAddPokemonClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("My Team").assertIsDisplayed()
    }
}
