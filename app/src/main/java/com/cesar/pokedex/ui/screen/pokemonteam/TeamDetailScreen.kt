package com.cesar.pokedex.ui.screen.pokemonteam

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.TeamAnalysis
import com.cesar.pokedex.domain.model.TeamSuggestion
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.WrappingRow
import com.cesar.pokedex.ui.component.typeColor
import com.cesar.pokedex.ui.screen.pokemonlist.ALL_POKEMON_TYPES
import kotlinx.coroutines.launch

private val TABS = listOf("Roster", "Analysis", "Suggest")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    onBackClick: () -> Unit,
    onAddPokemonClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState { TABS.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.team?.name ?: "Team") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> RosterTab(
                        members = uiState.team?.members ?: emptyList(),
                        onAddClick = onAddPokemonClick,
                        onMemberClick = onPokemonClick,
                        onRemoveMember = { pokemonId -> viewModel.onEvent(TeamDetailEvent.RemoveMember(pokemonId)) }
                    )
                    1 -> AnalysisTab(analysis = uiState.analysis)
                    2 -> SuggestTab(
                        suggestions = uiState.suggestions,
                        selectedEnemyTypes = uiState.selectedEnemyTypes,
                        teamSize = uiState.team?.members?.size ?: 0,
                        onToggleEnemyType = { type -> viewModel.onEvent(TeamDetailEvent.ToggleEnemyType(type)) },
                        onClearEnemyTypes = { viewModel.onEvent(TeamDetailEvent.ClearEnemyTypes) },
                        onAddSuggestion = { pokemonId -> viewModel.onEvent(TeamDetailEvent.AddSuggestion(pokemonId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterTab(
    members: List<Pokemon>,
    onAddClick: () -> Unit,
    onMemberClick: (Int) -> Unit,
    onRemoveMember: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val slots = members + List(6 - members.size) { null }
    val rows = slots.chunked(2)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { pokemon ->
                    if (pokemon != null) {
                        MemberCard(
                            pokemon = pokemon,
                            onClick = { onMemberClick(pokemon.id) },
                            onRemove = { onRemoveMember(pokemon.id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        EmptySlotCard(
                            onClick = onAddClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                )
            }
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            WrappingRow(
                horizontalSpacing = 4.dp,
                verticalSpacing = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                pokemon.types.forEach { TypeBadge(typeName = it) }
            }
        }
    }
}

@Composable
private fun EmptySlotCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Pokemon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnalysisTab(
    analysis: TeamAnalysis?,
    modifier: Modifier = Modifier
) {
    if (analysis == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Add Pokemon to see team analysis.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalysisSectionCard(title = "Defensive Weaknesses") {
            if (analysis.weaknesses.isEmpty()) {
                Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.weaknesses.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                        TypeCountBadge(type = type, count = count, isNegative = true)
                    }
                }
            }
        }

        AnalysisSectionCard(title = "Defensive Resistances") {
            if (analysis.resistances.isEmpty()) {
                Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.resistances.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                        TypeCountBadge(type = type, count = count, isNegative = false)
                    }
                }
            }
        }

        if (analysis.immunities.isNotEmpty()) {
            AnalysisSectionCard(title = "Immunities") {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.immunities.keys.forEach { type ->
                        TypeBadge(typeName = type.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        AnalysisSectionCard(title = "Offensive Coverage") {
            if (analysis.offensiveCoverage.isEmpty()) {
                Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.offensiveCoverage.forEach { type ->
                        TypeBadge(typeName = type.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        if (analysis.coverageGaps.isNotEmpty()) {
            AnalysisSectionCard(title = "Coverage Gaps") {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.coverageGaps.forEach { type ->
                        TypeBadge(typeName = type.replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2+ members weak, none resist",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (analysis.averageStats.isNotEmpty()) {
            AnalysisSectionCard(title = "Average Stats") {
                analysis.averageStats.entries.forEach { (stat, avg) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stat, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "%.1f".format(avg),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun TypeCountBadge(
    type: String,
    count: Int,
    isNegative: Boolean,
    modifier: Modifier = Modifier
) {
    val color = typeColor(type)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Text(
            text = "×$count",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun SuggestTab(
    suggestions: List<TeamSuggestion>,
    selectedEnemyTypes: Set<String>,
    teamSize: Int,
    onToggleEnemyType: (String) -> Unit,
    onClearEnemyTypes: () -> Unit,
    onAddSuggestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Select enemy types to find counters:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(ALL_POKEMON_TYPES) { type ->
                    val isSelected = type.lowercase() in selectedEnemyTypes
                    val color = typeColor(type)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color.copy(alpha = if (isSelected) 1f else 0.4f))
                            .clickable { onToggleEnemyType(type.lowercase()) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = type, style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
            if (selectedEnemyTypes.isNotEmpty()) {
                TextButton(onClick = onClearEnemyTypes) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        if (selectedEnemyTypes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Select one or more enemy types above.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (suggestions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No suitable suggestions found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions, key = { it.pokemon.id }) { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        teamFull = teamSize >= 6,
                        onAdd = { onAddSuggestion(suggestion.pokemon.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: TeamSuggestion,
    teamFull: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = suggestion.pokemon.imageUrl,
                contentDescription = suggestion.pokemon.name,
                modifier = Modifier.size(56.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.pokemon.name,
                    style = MaterialTheme.typography.titleSmall
                )
                WrappingRow(horizontalSpacing = 4.dp, verticalSpacing = 4.dp) {
                    suggestion.pokemon.types.forEach { TypeBadge(typeName = it) }
                }
                if (suggestion.coverageDetails.isNotEmpty()) {
                    Text(
                        text = suggestion.coverageDetails.take(2).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Score: ${suggestion.score}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!teamFull) {
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to team",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
