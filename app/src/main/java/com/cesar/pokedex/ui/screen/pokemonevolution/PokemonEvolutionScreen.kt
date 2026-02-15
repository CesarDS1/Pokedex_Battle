package com.cesar.pokedex.ui.screen.pokemonevolution

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cesar.pokedex.R
import com.cesar.pokedex.domain.model.EvolutionStage
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import com.cesar.pokedex.domain.model.PokemonVariety

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonEvolutionScreen(
    onBackClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonEvolutionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.evolutions_and_forms)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState) {
            is PokemonEvolutionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PokemonEvolutionUiState.Error -> {
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
                            onClick = { viewModel.loadEvolutionInfo() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            is PokemonEvolutionUiState.Success -> {
                EvolutionContent(
                    info = state.info,
                    currentPokemonId = state.currentPokemonId,
                    onPokemonClick = onPokemonClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun EvolutionContent(
    info: PokemonEvolutionInfo,
    currentPokemonId: Int,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.evolution_chain),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (info.evolutions.size <= 1) {
            Text(
                text = stringResource(R.string.does_not_evolve),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            EvolutionChainRow(
                evolutions = info.evolutions,
                currentPokemonId = currentPokemonId,
                onPokemonClick = onPokemonClick
            )
        }

        val altForms = info.varieties.filter { !it.isDefault }
        if (altForms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.alternate_forms),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            altForms.forEach { variety ->
                VarietyCard(
                    variety = variety,
                    onClick = { onPokemonClick(variety.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EvolutionChainRow(
    evolutions: List<EvolutionStage>,
    currentPokemonId: Int,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        evolutions.forEachIndexed { index, stage ->
            if (index > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stage.trigger,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            EvolutionStageCard(
                stage = stage,
                isCurrent = stage.id == currentPokemonId,
                onClick = { onPokemonClick(stage.id) }
            )
        }
    }
}

@Composable
private fun EvolutionStageCard(
    stage: EvolutionStage,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isCurrent) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable(enabled = !isCurrent, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = stage.imageUrl,
                contentDescription = stage.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stage.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "#${stage.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VarietyCard(
    variety: PokemonVariety,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = variety.imageUrl,
                contentDescription = variety.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = variety.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
