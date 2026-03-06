package com.cesar.pokedex.ui.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cesar.pokedex.ui.theme.PokedexTheme
import org.junit.Rule
import org.junit.Test

class TypeBadgeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun typeBadge_showsFireTypeName() {
        composeRule.setContent {
            PokedexTheme { TypeBadge(typeName = "fire") }
        }
        composeRule.onNodeWithText("fire", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun typeBadge_showsWaterTypeName() {
        composeRule.setContent {
            PokedexTheme { TypeBadge(typeName = "water") }
        }
        composeRule.onNodeWithText("water", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun typeBadge_showsGrassTypeName() {
        composeRule.setContent {
            PokedexTheme { TypeBadge(typeName = "grass") }
        }
        composeRule.onNodeWithText("grass", ignoreCase = true).assertIsDisplayed()
    }
}
