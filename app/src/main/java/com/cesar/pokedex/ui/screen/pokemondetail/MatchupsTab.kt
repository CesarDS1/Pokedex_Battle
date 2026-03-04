package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.domain.util.TypeEffectivenessChart
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.WrappingRow

@Composable
internal fun MatchupsTab(
    types: List<PokemonType>,
    modifier: Modifier = Modifier
) {
    val pokemonTypeKeys = remember(types) {
        types.map { it.apiName.ifBlank { it.name.lowercase() } }
    }
    val matchups = remember(pokemonTypeKeys) {
        TypeEffectivenessChart.allMatchupsFor(pokemonTypeKeys)
    }
    val weak = remember(matchups) {
        matchups.filter { it.value > 1f }.keys.sorted()
            .map { it.replaceFirstChar { c -> c.uppercase() } }
    }
    val resistant = remember(matchups) {
        matchups.filter { it.value < 1f && it.value > 0f }.keys.sorted()
            .map { it.replaceFirstChar { c -> c.uppercase() } }
    }
    val immune = remember(matchups) {
        matchups.filter { it.value == 0f }.keys.sorted()
            .map { it.replaceFirstChar { c -> c.uppercase() } }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Combined defensive matchups
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.defensive_matchups),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (weak.isNotEmpty()) {
                    MatchupSection(
                        label = stringResource(R.string.weak_to),
                        types = weak
                    )
                }
                if (resistant.isNotEmpty()) {
                    MatchupSection(
                        label = stringResource(R.string.resistant_to),
                        types = resistant
                    )
                }
                if (immune.isNotEmpty()) {
                    MatchupSection(
                        label = stringResource(R.string.immune_to),
                        types = immune
                    )
                }
            }
        }

        // Offensive matchups per type
        types.forEach { type ->
            if (type.strengths.isNotEmpty() || type.ineffective.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.offensive_matchups_format, type.name),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (type.strengths.isNotEmpty()) {
                            MatchupSection(
                                label = stringResource(R.string.strong_against),
                                types = type.strengths
                            )
                        }
                        if (type.ineffective.isNotEmpty()) {
                            MatchupSection(
                                label = stringResource(R.string.not_effective_against),
                                types = type.ineffective
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MatchupSection(label: String, types: List<String>) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    WrappingRow(
        horizontalSpacing = 4.dp,
        verticalSpacing = 4.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        types.forEach { TypeBadge(typeName = it) }
    }
}
