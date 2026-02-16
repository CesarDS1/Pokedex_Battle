package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.PokemonStat

@Composable
internal fun StatsTab(
    stats: List<PokemonStat>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.base_stats),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                stats.forEach { stat ->
                    StatRow(stat = stat)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (stats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stats.sumOf { it.baseStat }.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatRow(stat: PokemonStat, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stat.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stat.baseStat.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        LinearProgressIndicator(
            progress = { (stat.baseStat / 255f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(2f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
