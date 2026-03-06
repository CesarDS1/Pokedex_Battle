package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class AddPokemonToTeamContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", types = listOf("Grass", "Poison"), imageUrl = "")
    private val charmander = Pokemon(id = 4, name = "Charmander", types = listOf("Fire"), imageUrl = "")

    private val allTypes = listOf("Normal", "Fire", "Water", "Grass")

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                AddPokemonToTeamContent(
                    uiState = AddPokemonUiState(isLoading = true, allTypes = allTypes),
                    onEvent = {}
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun emptyList_showsNoResultsMessage() {
        composeRule.setContent {
            PokedexTheme {
                AddPokemonToTeamContent(
                    uiState = AddPokemonUiState(isLoading = false, pokemon = emptyList(), allTypes = allTypes),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("No Pokemon found.").assertIsDisplayed()
    }

    @Test
    fun pokemonList_showsNames() {
        composeRule.setContent {
            PokedexTheme {
                AddPokemonToTeamContent(
                    uiState = AddPokemonUiState(
                        isLoading = false,
                        pokemon = listOf(bulbasaur, charmander),
                        allTypes = allTypes
                    ),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }

    @Test
    fun searchBar_isDisplayed() {
        composeRule.setContent {
            PokedexTheme {
                AddPokemonToTeamContent(
                    uiState = AddPokemonUiState(isLoading = false, allTypes = allTypes),
                    onEvent = {}
                )
            }
        }
        composeRule.onNodeWithText("Search Pokemon…").assertIsDisplayed()
    }

    @Test
    fun typeFilterChips_areDisplayed() {
        composeRule.setContent {
            PokedexTheme {
                AddPokemonToTeamContent(
                    uiState = AddPokemonUiState(isLoading = false, allTypes = allTypes),
                    onEvent = {}
                )
            }
        }
        composeRule.onAllNodesWithText("Fire").assertCountEquals(2) // shown in both filter rows
        composeRule.onAllNodesWithText("Water").assertCountEquals(2)
    }
}
