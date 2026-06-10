package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.typeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPokemonToTeamScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddPokemonToTeamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddPokemonEvent.PopBack -> onBackClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pokemon") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AddPokemonToTeamContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AddPokemonToTeamContent(
    uiState: AddPokemonUiState,
    onEvent: (AddPokemonToTeamEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val scoreGroups = remember(uiState.pokemon, uiState.suggestions) {
        if (uiState.suggestions.isEmpty()) null
        else {
            val pos = uiState.pokemon.filter { (uiState.suggestions[it.id] ?: 0) > 0 }
            val neu = uiState.pokemon.filter { (uiState.suggestions[it.id] ?: 0) == 0 }
            val neg = uiState.pokemon.filter { (uiState.suggestions[it.id] ?: 0) < 0 }
            listOfNotNull(
                pos.takeIf { it.isNotEmpty() }?.let { Triple("Ventaja", true, it) },
                neu.takeIf { it.isNotEmpty() }?.let { Triple("Neutral", null, it) },
                neg.takeIf { it.isNotEmpty() }?.let { Triple("Desventaja", false, it) }
            )
        }
    }
    val collapsedGroups = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(AddPokemonToTeamEvent.Search(it)) },
            placeholder = {
                Text(
                    text = "Search Pokémon…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = uiState.searchQuery.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    IconButton(onClick = { onEvent(AddPokemonToTeamEvent.Search("")) }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            ),
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .focusRequester(focusRequester)
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {

                // — Filter by type ————————————————————————————————————————
                FilterSectionHeader(
                    label = stringResource(R.string.filter_by_type),
                    hasSelection = uiState.selectedTypes.isNotEmpty(),
                    onClear = { onEvent(AddPokemonToTeamEvent.ClearTypeFilters) }
                )
                LazyRow(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.allTypes) { type ->
                        TypeFilterPill(
                            type = type,
                            isSelected = type in uiState.selectedTypes,
                            onClick = { onEvent(AddPokemonToTeamEvent.ToggleTypeFilter(type)) }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // — Suggest vs ————————————————————————————————————————————
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f))
                ) {
                    Column {
                        FilterSectionHeader(
                            label = stringResource(R.string.suggest_vs),
                            hasSelection = uiState.selectedEnemyTypes.isNotEmpty(),
                            onClear = { onEvent(AddPokemonToTeamEvent.ClearEnemyTypes) },
                            labelColor = MaterialTheme.colorScheme.error
                        )
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.allTypes) { type ->
                                TypeFilterPill(
                                    type = type,
                                    isSelected = type.lowercase() in uiState.selectedEnemyTypes,
                                    onClick = { onEvent(AddPokemonToTeamEvent.ToggleEnemyType(type.lowercase())) }
                                )
                            }
                        }
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.pokemon.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No Pokemon found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (scoreGroups == null) {
                        items(uiState.pokemon, key = { it.id }) { pokemon ->
                            AddPokemonItem(
                                pokemon = pokemon,
                                score = null,
                                onAdd = { onEvent(AddPokemonToTeamEvent.AddPokemon(pokemon.id)) }
                            )
                        }
                    } else {
                        scoreGroups.forEach { (title, isPositive, group) ->
                            val isExpanded = collapsedGroups[title] != true
                            stickyHeader(key = title) {
                                SuggestionGroupHeader(
                                    title = title,
                                    count = group.size,
                                    isPositive = isPositive,
                                    isExpanded = isExpanded,
                                    onToggle = { collapsedGroups[title] = collapsedGroups[title] != true }
                                )
                            }
                            if (isExpanded) {
                                items(group, key = { it.id }) { pokemon ->
                                    AddPokemonItem(
                                        pokemon = pokemon,
                                        score = uiState.suggestions[pokemon.id] ?: 0,
                                        onAdd = { onEvent(AddPokemonToTeamEvent.AddPokemon(pokemon.id)) }
                                    )
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
private fun FilterSectionHeader(
    label: String,
    hasSelection: Boolean,
    onClear: () -> Unit,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
        AnimatedVisibility(visible = hasSelection) {
            TextButton(
                onClick = onClear,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Clear", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun TypeFilterPill(
    type: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = typeColor(type)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (isSelected) 1f else 0.35f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SuggestionGroupHeader(
    title: String,
    count: Int,
    isPositive: Boolean?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (isPositive) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFE53935)
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "chevronRotation"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0.15f), Color.Transparent)
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
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = color,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
    }
}

@Composable
private fun AddPokemonItem(
    pokemon: Pokemon,
    score: Int?,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDimmed = score != null && score < 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDimmed) 0.5f else 1f)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(64.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = pokemon.name, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { TypeBadge(typeName = it) }
            }
        }
        if (score != null && score != 0) {
            val scoreColor = if (score > 0) Color(0xFF4CAF50) else Color(0xFFE53935)
            Text(
                text = if (score > 0) "+$score" else "$score",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(scoreColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        IconButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add ${pokemon.name}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
