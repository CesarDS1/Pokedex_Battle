package com.cesar.pokedex.ui.screen.pokemoncompare

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.util.TypeEffectivenessChart
import com.cesar.pokedex.domain.util.toTypeKeys
import com.cesar.pokedex.ui.component.TypeBadge
import java.util.Locale

@Composable
fun PokemonCompareScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonCompareViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    PokemonCompareContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonCompareContent(
    uiState: PokemonCompareUiState,
    onEvent: (PokemonCompareEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compare)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PickSlotCard(
                    detail = uiState.slotA,
                    isLoading = uiState.isLoadingSlotA,
                    error = uiState.errorSlotA,
                    onPick = { onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.A)) },
                    onClear = { onEvent(PokemonCompareEvent.ClearSlot(ComparatorSlot.A)) },
                    modifier = Modifier.weight(1f),
                )
                PickSlotCard(
                    detail = uiState.slotB,
                    isLoading = uiState.isLoadingSlotB,
                    error = uiState.errorSlotB,
                    onPick = { onEvent(PokemonCompareEvent.OpenSlotPicker(ComparatorSlot.B)) },
                    onClear = { onEvent(PokemonCompareEvent.ClearSlot(ComparatorSlot.B)) },
                    modifier = Modifier.weight(1f),
                )
            }

            val slotA = uiState.slotA
            val slotB = uiState.slotB
            if (slotA != null && slotB != null) {
                Spacer(Modifier.height(16.dp))
                ComparatorStatsCard(pokemonA = slotA, pokemonB = slotB)
                Spacer(Modifier.height(16.dp))
                HeadToHeadMatchupCard(pokemonA = slotA, pokemonB = slotB)
            } else {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.compare_empty_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (uiState.activeSheetSlot != null) {
        PokemonPickerSheet(
            searchQuery = uiState.searchQuery,
            results = uiState.pickerResults,
            isRosterLoading = uiState.isRosterLoading,
            onSearch = { onEvent(PokemonCompareEvent.SearchInSheet(it)) },
            onPick = { onEvent(PokemonCompareEvent.PickPokemon(it)) },
            onDismiss = { onEvent(PokemonCompareEvent.CloseSlotPicker) },
        )
    }
}

@Composable
private fun PickSlotCard(
    detail: PokemonDetail?,
    isLoading: Boolean,
    error: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .height(160.dp)
                .clickable(enabled = detail == null && !isLoading, onClick = onPick),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator()
                detail != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        AsyncImage(
                            model = detail.imageUrl,
                            contentDescription = detail.name,
                            modifier = Modifier.size(64.dp),
                        )
                        Text(
                            text = detail.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            detail.types.forEach { TypeBadge(typeName = it.name) }
                        }
                    }
                }
                error != null -> {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.pick_pokemon),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparatorStatsCard(
    pokemonA: PokemonDetail,
    pokemonB: PokemonDetail,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stat_comparison),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            val statNames = (pokemonA.stats.map { it.name } + pokemonB.stats.map { it.name }).distinct()
            statNames.forEach { statName ->
                val valueA = pokemonA.stats.firstOrNull { it.name == statName }?.baseStat ?: 0
                val valueB = pokemonB.stats.firstOrNull { it.name == statName }?.baseStat ?: 0
                ComparatorStatRow(statName = statName, valueA = valueA, valueB = valueB)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ComparatorStatRow(
    statName: String,
    valueA: Int,
    valueB: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valueA.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (valueA >= valueB) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.width(32.dp),
            )
            LinearProgressIndicator(
                progress = { (valueA / 255f).coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                color = if (valueA >= valueB) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { (valueB / 255f).coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                color = if (valueB >= valueA) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = valueB.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (valueB >= valueA) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = Modifier.width(32.dp),
            )
        }
    }
}

@Composable
private fun HeadToHeadMatchupCard(
    pokemonA: PokemonDetail,
    pokemonB: PokemonDetail,
    modifier: Modifier = Modifier,
) {
    val matchup =
        remember(pokemonA.types, pokemonB.types) {
            TypeEffectivenessChart.headToHead(pokemonA.types.toTypeKeys(), pokemonB.types.toTypeKeys())
        }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.head_to_head),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            MatchupVerdictRow(
                label = stringResource(R.string.attacking_format, pokemonA.name, pokemonB.name),
                multiplier = matchup.aAttackingB,
            )
            Spacer(Modifier.height(8.dp))
            MatchupVerdictRow(
                label = stringResource(R.string.attacking_format, pokemonB.name, pokemonA.name),
                multiplier = matchup.bAttackingA,
            )
        }
    }
}

@Composable
private fun MatchupVerdictRow(
    label: String,
    multiplier: Float,
    modifier: Modifier = Modifier,
) {
    val color =
        when {
            multiplier > 1f -> Color(0xFFE53935)
            multiplier < 1f -> Color(0xFF4CAF50)
            else -> MaterialTheme.colorScheme.onSurface
        }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatMultiplier(multiplier),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private fun formatMultiplier(multiplier: Float): String {
    val trimmed =
        String
            .format(Locale.US, "%.2f", multiplier)
            .trimEnd('0')
            .trimEnd('.')
    return "$trimmed×"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonPickerSheet(
    searchQuery: String,
    results: List<Pokemon>,
    isRosterLoading: Boolean,
    onSearch: (String) -> Unit,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = searchQuery,
                onValueChange = onSearch,
                placeholder = { Text(stringResource(R.string.search_pokemon)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                isRosterLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                results.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_pokemon_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(results, key = { it.id }) { pokemon ->
                            PokemonPickerRow(pokemon = pokemon, onClick = { onPick(pokemon.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonPickerRow(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(48.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = pokemon.name, style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            pokemon.types.forEach { TypeBadge(typeName = it) }
        }
    }
}
