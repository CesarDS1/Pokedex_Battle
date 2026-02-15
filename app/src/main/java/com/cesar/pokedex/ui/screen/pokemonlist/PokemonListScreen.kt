package com.cesar.pokedex.ui.screen.pokemonlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cesar.pokedex.R
import coil.compose.AsyncImage
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.screen.pokemondetail.TypeBadge
import com.cesar.pokedex.ui.screen.pokemondetail.typeColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val collapsedGenerations by viewModel.collapsedGenerations.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.pokeball),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleShowFavoritesOnly) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites),
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search_pokemon)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TypeFilterRow(
                selectedTypes = selectedTypes,
                onToggleType = viewModel::toggleTypeFilter,
                onClearTypes = viewModel::clearTypeFilters
            )

            when (val state = uiState) {
                is PokemonListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PokemonListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.loadPokemon() },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                is PokemonListUiState.Success -> {
                    if (state.pokemonByGeneration.isEmpty()) {
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
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshPokemon() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                state.pokemonByGeneration.forEach { (generation, pokemonList) ->
                                    val isExpanded = generation !in collapsedGenerations
                                    stickyHeader(key = generation) {
                                        GenerationHeader(
                                            title = generation,
                                            isExpanded = isExpanded,
                                            onToggle = { viewModel.toggleGeneration(generation) }
                                        )
                                    }
                                    if (isExpanded) {
                                        items(pokemonList, key = { it.id }) { pokemon ->
                                            PokemonListItem(
                                                pokemon = pokemon,
                                                isFavorite = pokemon.id in favoriteIds,
                                                onClick = { onPokemonClick(pokemon.id) },
                                                onFavoriteClick = { viewModel.toggleFavorite(pokemon.id) }
                                            )
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
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "chevronRotation"
    )
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(if (isExpanded) R.string.collapse else R.string.expand),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotationAngle)
            )
        }
    }
}

@Composable
private fun PokemonListItem(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(96.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (pokemon.types.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { typeName ->
                        TypeBadge(typeName = typeName)
                    }
                }
            }
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.favorites),
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TypeFilterRow(
    selectedTypes: Set<String>,
    onToggleType: (String) -> Unit,
    onClearTypes: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedTypes.isNotEmpty()) {
            item {
                TextButton(onClick = onClearTypes) {
                    Text(stringResource(R.string.clear_filters))
                }
            }
        }
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
