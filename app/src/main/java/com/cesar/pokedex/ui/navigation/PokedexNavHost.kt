package com.cesar.pokedex.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.cesar.pokedex.R
import com.cesar.pokedex.ui.screen.about.AboutScreen
import com.cesar.pokedex.ui.screen.pokemondetail.PokemonDetailScreen
import com.cesar.pokedex.ui.screen.pokemonevolution.PokemonEvolutionScreen
import com.cesar.pokedex.ui.screen.pokemonlist.PokemonListScreen
import com.cesar.pokedex.ui.screen.pokemonmoves.PokemonMovesScreen
import com.cesar.pokedex.ui.screen.pokemonteam.AddPokemonToTeamScreen
import com.cesar.pokedex.ui.screen.pokemonteam.TeamDetailScreen
import com.cesar.pokedex.ui.screen.pokemonteam.TeamListScreen

private const val TRANSITION_DURATION = 300
private val TOP_LEVEL_ROUTES = setOf("pokemon_list", "team_list")

@Composable
fun PokedexNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        // Don't consume system insets — inner Scaffolds handle them individually
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "pokemon_list",
                        onClick = {
                            navController.navigate("pokemon_list") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Image(
                                painter = painterResource(R.drawable.pokeball),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Pokédex") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "team_list",
                        onClick = {
                            navController.navigate("team_list") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null
                            )
                        },
                        label = { Text("Teams") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "pokemon_list",
            modifier = modifier,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            }
        ) {
            composable("pokemon_list") {
                PokemonListScreen(
                    onPokemonClick = { pokemonId ->
                        navController.navigate("pokemon_detail/$pokemonId")
                    },
                    onAboutClick = {
                        navController.navigate("about")
                    },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
            composable(
                route = "pokemon_detail/{pokemonId}",
                arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
            ) {
                PokemonDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onEvolutionClick = { pokemonId ->
                        navController.navigate("pokemon_evolution/$pokemonId")
                    },
                    onMovesClick = { pokemonId ->
                        navController.navigate("pokemon_moves/$pokemonId")
                    }
                )
            }
            composable(
                route = "pokemon_evolution/{pokemonId}",
                arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
            ) {
                PokemonEvolutionScreen(
                    onBackClick = { navController.popBackStack() },
                    onPokemonClick = { pokemonId ->
                        navController.navigate("pokemon_detail/$pokemonId") {
                            popUpTo("pokemon_detail/{pokemonId}") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "pokemon_moves/{pokemonId}",
                arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
            ) {
                PokemonMovesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("team_list") {
                TeamListScreen(
                    onTeamClick = { teamId -> navController.navigate("team_detail/$teamId") },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
            composable(
                route = "team_detail/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.LongType })
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
                TeamDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddPokemonClick = {
                        navController.navigate("team_add_pokemon/$teamId")
                    },
                    onPokemonClick = { pokemonId ->
                        navController.navigate("pokemon_detail/$pokemonId")
                    }
                )
            }
            composable(
                route = "team_add_pokemon/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.LongType })
            ) {
                AddPokemonToTeamScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("about") {
                AboutScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
