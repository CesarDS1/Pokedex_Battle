package com.cesar.pokedex.ui.screen.pokemonlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.typeColor

@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit = {},
    onTeamsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    PokemonListContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onPokemonClick = onPokemonClick,
        onTeamsClick = onTeamsClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonListContent(
    uiState: PokemonListUiState,
    onEvent: (PokemonListEvent) -> Unit,
    onPokemonClick: (Int) -> Unit,
    onTeamsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val infiniteTransition = rememberInfiniteTransition(label = "pokeball_rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 3000, easing = LinearEasing)
                        ),
                        label = "pokeball_angle"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.pokeball),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).rotate(rotation)
                        )
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    IconButton(onClick = onTeamsClick) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Teams"
                        )
                    }
                    IconButton(onClick = { onEvent(PokemonListEvent.ToggleShowFavoritesOnly) }) {
                        Icon(
                            imageVector = if (uiState.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites),
                            tint = if (uiState.showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(PokemonListEvent.Search(it)) },
                    placeholder = { Text(stringResource(R.string.search_pokemon)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(PokemonListEvent.Search("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.filter_by_type),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.selectedTypes.isNotEmpty()) {
                            TextButton(
                                onClick = { onEvent(PokemonListEvent.ClearTypeFilters) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.clear_filters),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    TypeFilterRow(
                        selectedTypes = uiState.selectedTypes,
                        onToggleType = { onEvent(PokemonListEvent.ToggleTypeFilter(it)) }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 4.dp))
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { onEvent(PokemonListEvent.LoadPokemon) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                else -> {
                    if (uiState.pokemonByGeneration.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_pokemon_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { onEvent(PokemonListEvent.RefreshPokemon) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.pokemonByGeneration.forEach { (generation, pokemonList) ->
                                    val isExpanded = generation !in uiState.collapsedGenerations
                                    stickyHeader(key = generation) {
                                        GenerationHeader(
                                            title = generation,
                                            isExpanded = isExpanded,
                                            pokemonCount = pokemonList.size,
                                            onToggle = { onEvent(PokemonListEvent.ToggleGeneration(generation)) }
                                        )
                                    }
                                    if (isExpanded) {
                                        val rows = pokemonList.chunked(2)
                                        items(rows, key = { it.first().id }) { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                row.forEach { pokemon ->
                                                    PokemonGridCard(
                                                        pokemon = pokemon,
                                                        isFavorite = pokemon.id in uiState.favoriteIds,
                                                        onClick = { onPokemonClick(pokemon.id) },
                                                        onFavoriteClick = {
                                                            onEvent(
                                                                PokemonListEvent.ToggleFavorite(pokemon.id)
                                                            )
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (row.size < 2) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(8.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerationHeader(
    title: String,
    isExpanded: Boolean,
    pokemonCount: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "chevronRotation"
    )
    val parts = title.split(" — ")
    val generationLabel = parts.getOrElse(0) { title }
    val regionName = parts.getOrElse(1) { "" }
    val accentColor = regionAccentColor(regionName)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.18f), Color.Transparent)
                )
            )
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = generationLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    letterSpacing = 1.5.sp
                )
                if (regionName.isNotEmpty()) {
                    Text(
                        text = regionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "$pokemonCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(if (isExpanded) R.string.collapse else R.string.expand),
                tint = accentColor,
                modifier = Modifier.rotate(rotationAngle)
            )
        }
    }
}

@Composable
private fun regionAccentColor(region: String): Color = when (region.lowercase()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonGridCard(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryType = pokemon.types.firstOrNull() ?: "normal"
    val cardColor = typeColor(primaryType)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor.copy(alpha = 0.15f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorites),
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(cardColor.copy(alpha = 0.25f))
                )
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                pokemon.types.forEach { typeName ->
                    TypeBadge(typeName = typeName)
                }
            }
        }
    }
}

@Composable
private fun TypeFilterRow(
    selectedTypes: Set<String>,
    onToggleType: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ALL_POKEMON_TYPES) { type ->
            val isSelected = type in selectedTypes
            val color = typeColor(type)
            val alpha = if (isSelected) 1f else 0.4f
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = alpha))
                    .clickable { onToggleType(type) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}
