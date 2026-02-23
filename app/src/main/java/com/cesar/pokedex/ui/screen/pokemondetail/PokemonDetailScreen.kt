package com.cesar.pokedex.ui.screen.pokemondetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.GameEntry
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.WrappingRow
import com.cesar.pokedex.ui.component.typeColor
import kotlinx.coroutines.launch

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
                title = { Text(text = stringResource(R.string.pokemon_detail)) },
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
        stringResource(R.string.tab_matchups),
        stringResource(R.string.tab_games)
    )
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    val heroColor = typeColor(pokemon.types.firstOrNull()?.name ?: "Normal").copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(heroColor, Color.Transparent),
                    startY = 0f,
                    endY = 600f
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(200.dp),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                }
            )

            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (pokemon.cryUrl != null) {
                    IconButton(
                        onClick = { onPlayCry(pokemon.cryUrl) },
                        enabled = !isPlayingCry
                    ) {
                        AnimatedContent(
                            targetState = isPlayingCry,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "cryButton"
                        ) { playing ->
                            if (playing) {
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
                3 -> GamesTab(gameEntries = pokemon.gameEntries)
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
        WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 4.dp) {
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
            OutlinedButton(
                onClick = { onEvolutionClick(pokemon.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountTree,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.evolutions_and_forms))
            }
            OutlinedButton(
                onClick = { onMovesClick(pokemon.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.FlashOn,
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
            GenderBar(
                genderRate = pokemon.genderRate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GenderBar(genderRate: Int, modifier: Modifier = Modifier) {
    val maleColor   = Color(0xFF1E88E5)
    val femaleColor = Color(0xFFE91E63)
    when (genderRate) {
        -1 -> Text("Genderless", style = MaterialTheme.typography.bodyMedium)
        0  -> Text("100% Male", style = MaterialTheme.typography.bodyMedium, color = maleColor)
        8  -> Text("100% Female", style = MaterialTheme.typography.bodyMedium, color = femaleColor)
        else -> {
            val femaleRatio = genderRate / 8f
            val maleRatio   = 1f - femaleRatio
            Column(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        Modifier
                            .weight(maleRatio)
                            .fillMaxHeight()
                            .background(maleColor)
                    )
                    Box(
                        Modifier
                            .weight(femaleRatio)
                            .fillMaxHeight()
                            .background(femaleColor)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = "%.1f%% M".format(maleRatio * 100),
                        style = MaterialTheme.typography.labelSmall,
                        color = maleColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "%.1f%% F".format(femaleRatio * 100),
                        style = MaterialTheme.typography.labelSmall,
                        color = femaleColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GamesTab(gameEntries: List<GameEntry>) {
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
