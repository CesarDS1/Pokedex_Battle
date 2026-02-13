package com.cesar.pokedex.domain.repository

import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo

interface PokemonRepository {
    suspend fun getPokemonList(forceRefresh: Boolean = false): List<Pokemon>
    suspend fun getPokemonDetail(id: Int): PokemonDetail
    suspend fun getEvolutionInfo(id: Int): PokemonEvolutionInfo
}
