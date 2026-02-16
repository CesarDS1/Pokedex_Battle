package com.cesar.pokedex.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TypeBadge(typeName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(typeColor(typeName))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = typeName,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

fun typeColor(typeName: String): Color = when (typeName.lowercase()) {
    "normal" -> Color(0xFFA8A77A)
    "fire" -> Color(0xFFEE8130)
    "water" -> Color(0xFF6390F0)
    "electric" -> Color(0xFFF7D02C)
    "grass" -> Color(0xFF7AC74C)
    "ice" -> Color(0xFF96D9D6)
    "fighting" -> Color(0xFFC22E28)
    "poison" -> Color(0xFFA33EA1)
    "ground" -> Color(0xFFE2BF65)
    "flying" -> Color(0xFFA98FF3)
    "psychic" -> Color(0xFFF95587)
    "bug" -> Color(0xFFA6B91A)
    "rock" -> Color(0xFFB6A136)
    "ghost" -> Color(0xFF735797)
    "dragon" -> Color(0xFF6F35FC)
    "dark" -> Color(0xFF705746)
    "steel" -> Color(0xFFB7B7CE)
    "fairy" -> Color(0xFFD685AD)
    else -> Color(0xFF68A090)
}

@Composable
fun WrappingRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hSpacingPx = horizontalSpacing.roundToPx()
        val vSpacingPx = verticalSpacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

        var x = 0
        var y = 0
        var rowHeight = 0
        val positions = placeables.map { placeable ->
            if (x > 0 && x + placeable.width > constraints.maxWidth) {
                x = 0
                y += rowHeight + vSpacingPx
                rowHeight = 0
            }
            val pos = Pair(x, y)
            x += placeable.width + hSpacingPx
            rowHeight = maxOf(rowHeight, placeable.height)
            pos
        }

        val totalHeight = if (placeables.isEmpty()) 0 else y + rowHeight
        layout(constraints.maxWidth, totalHeight) {
            placeables.forEachIndexed { i, placeable ->
                placeable.placeRelative(positions[i].first, positions[i].second)
            }
        }
    }
}
