package com.cesar.pokedex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Composable
fun regionAccentColor(region: String): Color = when (region.lowercase()) {
    "kanto"  -> Color(0xFFE53935)
    "johto"  -> Color(0xFFFFB300)
    "hoenn"  -> Color(0xFF1E88E5)
    "sinnoh" -> Color(0xFF8E24AA)
    "unova"  -> Color(0xFF546E7A)
    "kalos"  -> Color(0xFF039BE5)
    "alola"  -> Color(0xFFFF8F00)
    "galar"  -> Color(0xFFD81B60)
    "paldea" -> Color(0xFF00897B)
    else     -> MaterialTheme.colorScheme.primary
}
