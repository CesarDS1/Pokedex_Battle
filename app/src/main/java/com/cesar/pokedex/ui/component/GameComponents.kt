package com.cesar.pokedex.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.GameEntry

@Composable
fun GamesTab(gameEntries: List<GameEntry>) {
    if (gameEntries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_game_entries),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gameEntries) { entry -> GameCard(entry) }
        }
    }
}

@Composable
private fun GameCard(entry: GameEntry) {
    Card(
        colors = CardDefaults.cardColors(containerColor = gameVersionColor(entry.gameName)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.height * 0.38f
                val cx = size.width - r - 8.dp.toPx()
                val cy = size.height / 2f
                val center = Offset(cx, cy)
                val stroke = Stroke(3.dp.toPx())
                drawCircle(Color.White.copy(alpha = 0.15f), r, center)
                drawCircle(Color.White.copy(alpha = 0.20f), r, center, style = stroke)
                drawArc(
                    Color.White.copy(alpha = 0.10f), 180f, 180f, true,
                    topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2)
                )
                drawLine(
                    Color.White.copy(alpha = 0.20f),
                    Offset(cx - r, cy), Offset(cx + r, cy), 3.dp.toPx()
                )
                val innerR = r * 0.25f
                drawCircle(Color.White.copy(alpha = 0.30f), innerR, center)
                drawCircle(Color.White.copy(alpha = 0.20f), innerR, center, style = stroke)
            }
            Text(
                text = entry.gameName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

private fun gameVersionColor(gameName: String): Color = when (gameName.lowercase()) {
    "red"               -> Color(0xFFE53935)
    "blue"              -> Color(0xFF1E88E5)
    "yellow"            -> Color(0xFFE6A817)
    "gold"              -> Color(0xFFFFB300)
    "silver"            -> Color(0xFF78909C)
    "crystal"           -> Color(0xFF00ACC1)
    "ruby"              -> Color(0xFFC62828)
    "sapphire"          -> Color(0xFF1565C0)
    "emerald"           -> Color(0xFF2E7D32)
    "firered"           -> Color(0xFFE64A19)
    "leafgreen"         -> Color(0xFF388E3C)
    "diamond"           -> Color(0xFF5C6BC0)
    "pearl"             -> Color(0xFFEC407A)
    "platinum"          -> Color(0xFF546E7A)
    "heartgold"         -> Color(0xFFF9A825)
    "soulsilver"        -> Color(0xFF90A4AE)
    "black"             -> Color(0xFF37474F)
    "white"             -> Color(0xFF78909C)
    "black 2"           -> Color(0xFF263238)
    "white 2"           -> Color(0xFF607D8B)
    "x"                 -> Color(0xFF1976D2)
    "y"                 -> Color(0xFFD32F2F)
    "omega ruby"        -> Color(0xFFB71C1C)
    "alpha sapphire"    -> Color(0xFF0D47A1)
    "sun"               -> Color(0xFFFF8F00)
    "moon"              -> Color(0xFF283593)
    "ultra sun"         -> Color(0xFFE65100)
    "ultra moon"        -> Color(0xFF1A237E)
    "lets go pikachu"   -> Color(0xFFF9A825)
    "lets go eevee"     -> Color(0xFF8D6E63)
    "sword"             -> Color(0xFF1565C0)
    "shield"            -> Color(0xFFAD1457)
    "brilliant diamond" -> Color(0xFF3949AB)
    "shining pearl"     -> Color(0xFFC2185B)
    "legends arceus"    -> Color(0xFF4E342E)
    "scarlet"           -> Color(0xFFC62828)
    "violet"            -> Color(0xFF6A1B9A)
    else                -> Color(0xFF616161)
}
