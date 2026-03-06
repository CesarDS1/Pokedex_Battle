package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class PokemonDetailContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakePokemon = PokemonDetail(
        id = 6,
        name = "Charizard",
        imageUrl = "",
        description = "A flame pokemon.",
        region = "Kanto",
        heightDecimeters = 17,
        weightHectograms = 905,
        genderRate = 1,
        types = listOf(
            PokemonType(
                name = "fire",
                apiName = "fire",
                weaknesses = listOf("water", "rock", "electric"),
                resistances = listOf("fire", "grass", "bug", "steel", "fairy"),
                strengths = listOf("grass", "ice", "bug", "steel"),
                ineffective = listOf()
            )
        ),
        abilities = listOf(Ability(name = "Blaze", isHidden = false)),
        stats = emptyList(),
        gameEntries = emptyList()
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                PokemonDetailScreenContent(
                    uiState = PokemonDetailUiState.Loading,
                    onEvent = {},
                    onBackClick = {},
                    onEvolutionClick = {},
                    onMovesClick = {}
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        composeRule.setContent {
            PokedexTheme {
                PokemonDetailScreenContent(
                    uiState = PokemonDetailUiState.Error("Failed to load"),
                    onEvent = {},
                    onBackClick = {},
                    onEvolutionClick = {},
                    onMovesClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Failed to load").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_showsPokemonName() {
        composeRule.setContent {
            PokedexTheme {
                PokemonDetailScreenContent(
                    uiState = PokemonDetailUiState.Success(pokemon = fakePokemon),
                    onEvent = {},
                    onBackClick = {},
                    onEvolutionClick = {},
                    onMovesClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Charizard").assertIsDisplayed()
    }

    @Test
    fun successState_tabsAreDisplayed() {
        composeRule.setContent {
            PokedexTheme {
                PokemonDetailScreenContent(
                    uiState = PokemonDetailUiState.Success(pokemon = fakePokemon),
                    onEvent = {},
                    onBackClick = {},
                    onEvolutionClick = {},
                    onMovesClick = {}
                )
            }
        }
        composeRule.onNodeWithText("About").assertIsDisplayed()
        composeRule.onNodeWithText("Stats").assertIsDisplayed()
        composeRule.onNodeWithText("Matchups").assertIsDisplayed()
        composeRule.onNodeWithText("Games").assertIsDisplayed()
    }

    @Test
    fun favoriteIcon_reflectsUnfavoritedState() {
        composeRule.setContent {
            PokedexTheme {
                PokemonDetailScreenContent(
                    uiState = PokemonDetailUiState.Success(pokemon = fakePokemon, isFavorite = false),
                    onEvent = {},
                    onBackClick = {},
                    onEvolutionClick = {},
                    onMovesClick = {}
                )
            }
        }
        // When not favorite, the FavoriteBorder icon is shown with contentDescription "Favorites"
        composeRule.onNodeWithContentDescription("Favorites").assertIsDisplayed()
    }
}
