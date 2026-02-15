package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonStat
import com.cesar.pokedex.domain.model.PokemonType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    onBackClick: () -> Unit,
    onEvolutionClick: (Int) -> Unit,
    onMovesClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlayingCry by viewModel.isPlayingCry.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val state = uiState) {
                        is PokemonDetailUiState.Success -> state.pokemon.name
                        else -> stringResource(R.string.pokemon_detail)
                    }
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites),
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState) {
            is PokemonDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PokemonDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadPokemonDetail() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            is PokemonDetailUiState.Success -> {
                PokemonDetailContent(
                    pokemon = state.pokemon,
                    onEvolutionClick = onEvolutionClick,
                    onMovesClick = onMovesClick,
                    isPlayingCry = isPlayingCry,
                    onPlayCry = { url -> viewModel.playCry(url) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetail,
    onEvolutionClick: (Int) -> Unit,
    onMovesClick: (Int) -> Unit,
    isPlayingCry: Boolean,
    onPlayCry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        stringResource(R.string.tab_about),
        stringResource(R.string.tab_stats),
        stringResource(R.string.tab_matchups)
    )
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(200.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${pokemon.name} #${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (pokemon.cryUrl != null) {
                    IconButton(
                        onClick = { onPlayCry(pokemon.cryUrl) },
                        enabled = !isPlayingCry
                    ) {
                        if (isPlayingCry) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.play_cry)
                            )
                        }
                    }
                }
            }

            if (pokemon.region != "Unknown") {
                Text(
                    text = stringResource(R.string.region_format, pokemon.region),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AboutTab(
                    pokemon = pokemon,
                    onEvolutionClick = onEvolutionClick,
                    onMovesClick = onMovesClick
                )
                1 -> StatsTab(stats = pokemon.stats)
                2 -> MatchupsTab(types = pokemon.types)
            }
        }
    }
}

@Composable
private fun AboutTab(
    pokemon: PokemonDetail,
    onEvolutionClick: (Int) -> Unit,
    onMovesClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pokemon.types.forEach { type ->
                TypeBadge(typeName = type.name)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PokemonInfoCard(pokemon = pokemon)

        Spacer(modifier = Modifier.height(12.dp))

        if (pokemon.description.isNotBlank()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pokemon.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { onEvolutionClick(pokemon.id) }) {
                Text(stringResource(R.string.evolutions_and_forms))
            }
            OutlinedButton(onClick = { onMovesClick(pokemon.id) }) {
                Text(stringResource(R.string.moves_by_level))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PokemonInfoCard(pokemon: PokemonDetail, modifier: Modifier = Modifier) {
    val heightM = pokemon.heightDecimeters / 10f
    val weightKg = pokemon.weightHectograms / 10f

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.height),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.height_format, heightM),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.weight),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.weight_format, weightKg),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = stringResource(R.string.abilities),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pokemon.abilities.forEach { ability ->
                    val label = if (ability.isHidden) "${ability.name} (${stringResource(R.string.hidden)})" else ability.name
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = stringResource(R.string.gender),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatGender(pokemon.genderRate),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatGender(genderRate: Int): String = when (genderRate) {
    -1 -> "Genderless"
    0 -> "100% Male"
    8 -> "100% Female"
    else -> {
        val femalePercent = genderRate * 12.5f
        val malePercent = 100f - femalePercent
        "%.1f%% Male, %.1f%% Female".format(malePercent, femalePercent)
    }
}

@Composable
private fun StatsTab(
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

@Composable
private fun MatchupsTab(
    types: List<PokemonType>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        types.forEach { type ->
            TypeMatchupCard(type = type)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TypeMatchupCard(type: PokemonType, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.type_matchups_format, type.name),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (type.weaknesses.isNotEmpty()) {
                MatchupSection(label = stringResource(R.string.weak_to), types = type.weaknesses)
            }
            if (type.resistances.isNotEmpty()) {
                MatchupSection(label = stringResource(R.string.resistant_to), types = type.resistances)
            }
            if (type.strengths.isNotEmpty()) {
                MatchupSection(label = stringResource(R.string.strong_against), types = type.strengths)
            }
            if (type.ineffective.isNotEmpty()) {
                MatchupSection(label = stringResource(R.string.not_effective_against), types = type.ineffective)
            }
        }
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

@Composable
private fun WrappingRow(
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

@Composable
internal fun TypeBadge(typeName: String, modifier: Modifier = Modifier) {
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

internal fun typeColor(typeName: String): Color = when (typeName.lowercase()) {
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
