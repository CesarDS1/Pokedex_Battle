package com.cesar.pokedex.ui.screen.pokemonteam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.ui.component.typeColor

@Composable
fun TeamListScreen(
    onTeamClick: (Long) -> Unit,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
    viewModel: TeamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TeamNavigationEvent.NavigateToTeamDetail -> onTeamClick(event.teamId)
            }
        }
    }

    TeamListContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        bottomPadding = bottomPadding,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamListContent(
    uiState: TeamListUiState,
    onEvent: (TeamListEvent) -> Unit,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Teams") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TeamListEvent.ShowCreateDialog) },
                modifier = Modifier.padding(bottom = bottomPadding)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create team")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.teams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No teams yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create your first team with the + button",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + bottomPadding
                    )
                ) {
                    items(uiState.teams, key = { it.id }) { team ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    onEvent(TeamListEvent.RequestDelete(team))
                                    true
                                } else false
                            }
                        )
                        LaunchedEffect(uiState.pendingDeleteTeam) {
                            if (uiState.pendingDeleteTeam == null) dismissState.reset()
                        }
                        SwipeToDeleteTeamCard(
                            teamName = team.name,
                            members = team.members,
                            onClick = { onEvent(TeamListEvent.NavigateToTeam(team.id)) },
                            dismissState = dismissState
                        )
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateTeamDialog(
                onDismiss = { onEvent(TeamListEvent.DismissCreateDialog) },
                onCreate = { name -> onEvent(TeamListEvent.CreateTeam(name)) }
            )
        }

        uiState.pendingDeleteTeam?.let { team ->
            DeleteConfirmationDialog(
                teamName = team.name,
                onConfirm = { onEvent(TeamListEvent.ConfirmDelete) },
                onDismiss = { onEvent(TeamListEvent.CancelDelete) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTeamCard(
    teamName: String,
    members: List<Pokemon>,
    onClick: () -> Unit,
    dismissState: SwipeToDismissBoxState,
    modifier: Modifier = Modifier
) {
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete team",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier
    ) {
        TeamCard(
            teamName = teamName,
            members = members,
            onClick = onClick
        )
    }
}

@Composable
private fun TeamCard(
    teamName: String,
    members: List<Pokemon>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(members) {
        members.firstOrNull()?.types?.firstOrNull()?.let { typeColor(it) }
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(accentColor)
                )
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val slotDesc = "${members.size} of 6 slots filled"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.semantics { contentDescription = slotDesc }
                    ) {
                        repeat(6) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < members.size) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }
                if (members.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        members.forEach { member ->
                            AsyncImage(
                                model = member.imageUrl,
                                contentDescription = member.name,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Team") },
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
                onClick = { if (teamName.isNotBlank()) onCreate(teamName.trim()) },
                enabled = teamName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    teamName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete team?") },
        text = { Text("\"$teamName\" will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
