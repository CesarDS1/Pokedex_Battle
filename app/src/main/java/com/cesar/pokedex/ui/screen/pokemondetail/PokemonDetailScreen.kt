package com.cesar.pokedex.ui.screen.pokemondetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.ui.component.TypeBadge

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

    val isFavorite = (uiState as? PokemonDetailUiState.Success)?.isFavorite ?: false

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
                    IconButton(onClick = { viewModel.onEvent(PokemonDetailEvent.ToggleFavorite) }) {
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
                            onClick = { viewModel.onEvent(PokemonDetailEvent.LoadDetail) },
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
                    isPlayingCry = state.isPlayingCry,
                    onPlayCry = { url -> viewModel.onEvent(PokemonDetailEvent.PlayCry(url)) },
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { onEvolutionClick(pokemon.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.evolutions_and_forms))
            }
            FilledTonalButton(
                onClick = { onMovesClick(pokemon.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
