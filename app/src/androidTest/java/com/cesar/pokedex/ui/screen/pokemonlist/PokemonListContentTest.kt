package com.cesar.pokedex.ui.screen.pokemonlist

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class PokemonListContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val bulbasaur = Pokemon(id = 1, name = "Bulbasaur", types = listOf("grass", "poison"), imageUrl = "")
    private val charmander = Pokemon(id = 4, name = "Charmander", types = listOf("fire"), imageUrl = "")

    private val loadedState = PokemonListUiState(
        pokemonByGeneration = mapOf(
            "Generation I — Kanto" to listOf(bulbasaur, charmander)
        )
    )

    @Test
    fun loadingState_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = PokemonListUiState(isLoading = true),
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun pokemonList_showsNames() {
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = loadedState,
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }

    @Test
    fun searchQuery_filtersResults() {
        val filteredState = PokemonListUiState(
            pokemonByGeneration = mapOf(
                "Generation I — Kanto" to listOf(charmander)
            ),
            searchQuery = "char"
        )
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = filteredState,
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
        composeRule.onAllNodesWithText("Bulbasaur").assertCountEquals(0)
    }

    @Test
    fun typeFilter_chipsAreDisplayed() {
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = loadedState,
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        // ALL_POKEMON_TYPES includes "Fire" and "Water" — verify the filter row shows them
        composeRule.onNodeWithText("Fire").assertIsDisplayed()
        composeRule.onNodeWithText("Water").assertIsDisplayed()
    }

    @Test
    fun favoritesOnly_filtersNonFavorites() {
        val favoritesState = PokemonListUiState(
            pokemonByGeneration = mapOf(
                "Generation I — Kanto" to listOf(bulbasaur)
            ),
            showFavoritesOnly = true,
            favoriteIds = setOf(1)
        )
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = favoritesState,
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onAllNodesWithText("Charmander").assertCountEquals(0)
    }

    @Test
    fun errorState_showsMessage() {
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = PokemonListUiState(errorMessage = "Network error"),
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Network error").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun emptySearch_showsAllPokemon() {
        composeRule.setContent {
            PokedexTheme {
                PokemonListContent(
                    uiState = loadedState.copy(searchQuery = ""),
                    onEvent = {},
                    onPokemonClick = {},
                    onTeamsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }
}
