package com.cesar.pokedex.ui.screen.pokemonevolution

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.EvolutionStage
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class PokemonEvolutionContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bulbasaur = EvolutionStage(id = 1, name = "Bulbasaur", imageUrl = "", trigger = "")
    private val ivysaur = EvolutionStage(id = 2, name = "Ivysaur", imageUrl = "", trigger = "Level 16")
    private val venusaur = EvolutionStage(id = 3, name = "Venusaur", imageUrl = "", trigger = "Level 32")

    private val evolutionInfo = PokemonEvolutionInfo(
        evolutions = listOf(bulbasaur, ivysaur, venusaur),
        varieties = emptyList()
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                EvolutionScreenContent(
                    uiState = PokemonEvolutionUiState.Loading,
                    onRetry = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun errorState_showsMessageAndRetry() {
        composeRule.setContent {
            PokedexTheme {
                EvolutionScreenContent(
                    uiState = PokemonEvolutionUiState.Error("Failed to load"),
                    onRetry = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Failed to load").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_showsEvolutionNames() {
        composeRule.setContent {
            PokedexTheme {
                EvolutionScreenContent(
                    uiState = PokemonEvolutionUiState.Success(evolutionInfo, currentPokemonId = 1),
                    onRetry = {},
                    onPokemonClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Ivysaur").assertIsDisplayed()
        composeRule.onNodeWithText("Venusaur").assertIsDisplayed()
    }

    @Test
    fun successState_clickOnEvolution_firesCallback() {
        var clickedId = -1
        composeRule.setContent {
            PokedexTheme {
                EvolutionScreenContent(
                    uiState = PokemonEvolutionUiState.Success(evolutionInfo, currentPokemonId = 1),
                    onRetry = {},
                    onPokemonClick = { clickedId = it }
                )
            }
        }
        // Ivysaur (id=2) is not the current pokemon, so clicking it should fire the callback
        composeRule.onNodeWithText("Ivysaur").performClick()
        assert(clickedId == 2)
    }
}
