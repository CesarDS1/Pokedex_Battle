package com.cesar.pokedex.ui.screen.pokemonteam

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.TeamAnalysis
import com.cesar.pokedex.ui.component.TypeBadge
import com.cesar.pokedex.ui.component.WrappingRow
import com.cesar.pokedex.ui.component.typeColor
import kotlinx.coroutines.launch

private val TABS = listOf("Roster", "Analysis")

@Composable
fun TeamDetailScreen(
    onBackClick: () -> Unit,
    onAddPokemonClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    TeamDetailScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onAddPokemonClick = onAddPokemonClick,
        onPokemonClick = onPokemonClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamDetailScreenContent(
    uiState: TeamDetailUiState,
    onEvent: (TeamDetailEvent) -> Unit,
    onBackClick: () -> Unit,
    onAddPokemonClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
                },
                actions = {
                    IconButton(onClick = { onEvent(TeamDetailEvent.ShowRenameDialog) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename team"
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
                        onRemoveMember = { pokemonId -> onEvent(TeamDetailEvent.RemoveMember(pokemonId)) },
                        onSwapMembers = { from, to -> onEvent(TeamDetailEvent.SwapMembers(from, to)) }
                    )
                    1 -> AnalysisTab(
                        analysis = uiState.analysis,
                        isLoading = uiState.team?.members?.isNotEmpty() == true && uiState.analysis == null
                    )
                }
            }
        }
    }

    if (uiState.showRenameDialog) {
        RenameTeamDialog(
            currentName = uiState.team?.name ?: "",
            onDismiss = { onEvent(TeamDetailEvent.DismissRenameDialog) },
            onConfirm = { name -> onEvent(TeamDetailEvent.ConfirmRename(name)) }
        )
    }
}

@Composable
private fun RosterTab(
    members: List<Pokemon>,
    onAddClick: () -> Unit,
    onMemberClick: (Int) -> Unit,
    onRemoveMember: (Int) -> Unit,
    onSwapMembers: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isReorderMode by remember { mutableStateOf(false) }
    LaunchedEffect(members.size) { isReorderMode = false }

    val indexedSlots = remember(members) {
        val filled: List<Pair<Int, Pokemon?>> = members.mapIndexed { i, p -> i to p }
        val empty: List<Pair<Int, Pokemon?>> = List(6 - members.size) { -1 to null }
        (filled + empty).chunked(2)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconToggleButton(
                checked = isReorderMode,
                onCheckedChange = { isReorderMode = it }
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Reorder members",
                    tint = if (isReorderMode) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        indexedSlots.forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { (memberIndex, pokemon) ->
                    if (pokemon != null) {
                        MemberCard(
                            pokemon = pokemon,
                            memberIndex = memberIndex,
                            totalMembers = members.size,
                            isReorderMode = isReorderMode,
                            onClick = { onMemberClick(pokemon.id) },
                            onRemove = { onRemoveMember(pokemon.id) },
                            onMoveLeft = { onSwapMembers(memberIndex, memberIndex - 1) },
                            onMoveRight = { onSwapMembers(memberIndex, memberIndex + 1) },
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
    memberIndex: Int,
    totalMembers: Int,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
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
                if (isReorderMode) {
                    IconButton(
                        onClick = onMoveLeft,
                        enabled = memberIndex > 0,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Move left",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveRight,
                        enabled = memberIndex < totalMembers - 1,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Move right",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
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
                }
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                )
            }
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
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
                .drawBehind {
                    val stroke = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.35f),
                        style = stroke,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Pokemon",
                    modifier = Modifier.size(32.dp),
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
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (analysis == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Add Pokemon to see team analysis.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val sortedWeaknesses = remember(analysis.weaknesses) {
        analysis.weaknesses.entries.sortedByDescending { it.value }
    }
    val sortedResistances = remember(analysis.resistances) {
        analysis.resistances.entries.sortedByDescending { it.value }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalysisSectionCard(title = "Defensive Weaknesses", icon = Icons.Default.Warning) {
            if (sortedWeaknesses.isEmpty()) {
                Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    sortedWeaknesses.forEach { (type, count) ->
                        TypeCountBadge(type = type, count = count, isNegative = true)
                    }
                }
            }
        }

        AnalysisSectionCard(title = "Defensive Resistances", icon = Icons.Default.Shield) {
            if (sortedResistances.isEmpty()) {
                Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    sortedResistances.forEach { (type, count) ->
                        TypeCountBadge(type = type, count = count, isNegative = false)
                    }
                }
            }
        }

        if (analysis.immunities.isNotEmpty()) {
            AnalysisSectionCard(title = "Immunities", icon = Icons.Default.Block) {
                WrappingRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    analysis.immunities.keys.forEach { type ->
                        TypeBadge(typeName = type.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        AnalysisSectionCard(title = "Offensive Coverage", icon = Icons.Default.FlashOn) {
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
            AnalysisSectionCard(title = "Coverage Gaps", icon = Icons.Default.ReportProblem) {
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
            AnalysisSectionCard(title = "Average Stats", icon = Icons.Default.BarChart) {
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
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
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
private fun RenameTeamDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var teamName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Team") },
        text = {
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (teamName.isNotBlank()) onConfirm(teamName.trim()) },
                enabled = teamName.isNotBlank()
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
