package com.cesar.pokedex.domain.repository

import com.cesar.pokedex.domain.model.PokemonTeam
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getAllTeams(): Flow<List<PokemonTeam>>
    suspend fun createTeam(name: String): Long
    suspend fun renameTeam(id: Long, name: String)
    suspend fun addMember(teamId: Long, pokemonId: Int)
    suspend fun removeMember(teamId: Long, pokemonId: Int)
    suspend fun deleteTeam(id: Long)
}
