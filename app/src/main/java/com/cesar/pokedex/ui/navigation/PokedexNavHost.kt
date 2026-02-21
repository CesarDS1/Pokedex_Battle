package com.cesar.pokedex.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cesar.pokedex.ui.screen.pokemondetail.PokemonDetailScreen
import com.cesar.pokedex.ui.screen.pokemonevolution.PokemonEvolutionScreen
import com.cesar.pokedex.ui.screen.pokemonlist.PokemonListScreen
import com.cesar.pokedex.ui.screen.pokemonmoves.PokemonMovesScreen
import com.cesar.pokedex.ui.screen.pokemonteam.AddPokemonToTeamScreen
import com.cesar.pokedex.ui.screen.pokemonteam.TeamDetailScreen
import com.cesar.pokedex.ui.screen.pokemonteam.TeamListScreen

private const val TRANSITION_DURATION = 300

@Composable
fun PokedexNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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
                onTeamsClick = {
                    navController.navigate("team_list")
                }
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
                onBackClick = { navController.popBackStack() },
                onTeamClick = { teamId -> navController.navigate("team_detail/$teamId") }
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
    }
}
