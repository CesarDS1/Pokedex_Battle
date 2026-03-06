package com.cesar.pokedex.ui.screen.pokemonmoves

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Move
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class PokemonMovesContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleMoves = listOf(
        Move(name = "Tackle", level = 1, type = "Normal", description = "A physical attack."),
        Move(name = "Ember", level = 7, type = "Fire", description = "A weak fire attack.")
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                MovesScreenContent(
                    uiState = PokemonMovesUiState.Loading,
                    onRetry = {}
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun errorState_showsMessageAndRetry() {
        composeRule.setContent {
            PokedexTheme {
                MovesScreenContent(
                    uiState = PokemonMovesUiState.Error("Network error"),
                    onRetry = {}
                )
            }
        }
        composeRule.onNodeWithText("Network error").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_showsMoveNames() {
        composeRule.setContent {
            PokedexTheme {
                MovesScreenContent(
                    uiState = PokemonMovesUiState.Success(sampleMoves),
                    onRetry = {}
                )
            }
        }
        composeRule.onNodeWithText("Tackle").assertIsDisplayed()
        composeRule.onNodeWithText("Ember").assertIsDisplayed()
    }

    @Test
    fun successState_showsMoveLevels() {
        composeRule.setContent {
            PokedexTheme {
                MovesScreenContent(
                    uiState = PokemonMovesUiState.Success(sampleMoves),
                    onRetry = {}
                )
            }
        }
        composeRule.onNodeWithText("Level 1").assertIsDisplayed()
        composeRule.onNodeWithText("Level 7").assertIsDisplayed()
    }
}
