package com.cesar.pokedex.domain.repository

import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getPokemonList(forceRefresh: Boolean = false): List<Pokemon>
    suspend fun getPokemonDetail(id: Int): PokemonDetail
    suspend fun getEvolutionInfo(id: Int): PokemonEvolutionInfo
    fun getFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(pokemonId: Int)
}
