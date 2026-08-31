package com.cesar.pokedex.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.background
import androidx.compose.foundation.style.shape
import androidx.compose.foundation.style.textStyle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val StyleScope.colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

val StyleScope.typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography

object PokedexStyles {
    val typeBadgeStyle = Style {
        shape(RoundedCornerShape(16.dp))
        padding(horizontal = 12.dp, vertical = 4.dp)
        textStyle(typography.labelMedium)
    }
}
