package com.cesar.pokedex.ui.navigation

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

@Composable
fun PokedexNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "pokemon_list",
        modifier = modifier
    ) {
        composable("pokemon_list") {
            PokemonListScreen(
                onPokemonClick = { pokemonId ->
                    navController.navigate("pokemon_detail/$pokemonId")
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
                onBackClick = { navController.popBackStack() }
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
    }
}
