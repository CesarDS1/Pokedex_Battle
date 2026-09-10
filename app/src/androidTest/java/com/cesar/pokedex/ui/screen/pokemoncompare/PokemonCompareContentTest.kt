package com.cesar.pokedex.ui.screen.pokemoncompare

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonStat
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class PokemonCompareContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fireType =
        PokemonType(
            name = "Fire",
            apiName = "fire",
            weaknesses = emptyList(),
            resistances = emptyList(),
            strengths = emptyList(),
            ineffective = emptyList(),
        )
    private val waterType =
        PokemonType(
            name = "Water",
            apiName = "water",
            weaknesses = emptyList(),
            resistances = emptyList(),
            strengths = emptyList(),
            ineffective = emptyList(),
        )

    private val charmander =
        PokemonDetail(
            id = 4,
            name = "Charmander",
            imageUrl = "",
            description = "",
            region = "Kanto",
            types = listOf(fireType),
            abilities = emptyList(),
            stats = listOf(PokemonStat(name = "hp", baseStat = 39), PokemonStat(name = "attack", baseStat = 52)),
        )
    private val squirtle =
        PokemonDetail(
            id = 7,
            name = "Squirtle",
            imageUrl = "",
            description = "",
            region = "Kanto",
            types = listOf(waterType),
            abilities = emptyList(),
            stats = listOf(PokemonStat(name = "hp", baseStat = 44), PokemonStat(name = "attack", baseStat = 48)),
        )

    @Test
    fun emptyState_showsTwoEmptySlotCards() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(uiState = PokemonCompareUiState(), onEvent = {}, onBackClick = {})
            }
        }
        composeRule.onAllNodesWithText("Pick Pokémon").assertCountEquals(2)
    }

    @Test
    fun loadingSlot_showsProgressIndicator() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState = PokemonCompareUiState(isLoadingSlotA = true),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun filledSlots_showsStatComparisonRows() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState = PokemonCompareUiState(slotA = charmander, slotB = squirtle),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Stat Comparison").assertIsDisplayed()
        composeRule.onNodeWithText("hp").assertIsDisplayed()
    }

    @Test
    fun filledSlots_showsHeadToHeadMatchupText() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState = PokemonCompareUiState(slotA = charmander, slotB = squirtle),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Head-to-Head").assertIsDisplayed()
    }

    @Test
    fun filledSlots_formatsWholeNumberMultiplierWithoutTrailingDecimal() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState = PokemonCompareUiState(slotA = charmander, slotB = squirtle),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        // Water (squirtle) attacking Fire (charmander) is a clean 2x multiplier.
        composeRule.onNodeWithText("2×").assertIsDisplayed()
    }

    @Test
    fun statUniqueToOneSlot_isStillShown() {
        val onlyHasDefense =
            squirtle.copy(stats = listOf(PokemonStat(name = "defense", baseStat = 65)))
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState = PokemonCompareUiState(slotA = charmander, slotB = onlyHasDefense),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        // "defense" only exists on slotB's stats, not slotA's - it must still render.
        composeRule.onNodeWithText("defense").assertIsDisplayed()
    }

    @Test
    fun sheetOpen_showsSearchFieldAndPickerResults() {
        composeRule.setContent {
            PokedexTheme {
                PokemonCompareContent(
                    uiState =
                        PokemonCompareUiState(
                            activeSheetSlot = ComparatorSlot.A,
                            isRosterLoading = false,
                            pickerResults = listOf(Pokemon(id = 4, name = "Charmander", imageUrl = "")),
                        ),
                    onEvent = {},
                    onBackClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Search Pokemon…").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }
}
