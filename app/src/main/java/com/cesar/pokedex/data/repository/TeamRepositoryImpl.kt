package com.cesar.pokedex.data.repository

import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.dao.TeamDao
import com.cesar.pokedex.data.local.entity.PokemonTeamEntity
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonTeam
import com.cesar.pokedex.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val pokemonDao: PokemonDao
) : TeamRepository {

    override fun getAllTeams(): Flow<List<PokemonTeam>> =
        teamDao.getAllTeams().map { entities ->
            entities.map { entity ->
                val members = entity.pokemonIds.mapNotNull { pokemonId ->
                    pokemonDao.getPokemonById(pokemonId)?.let { pokemonEntity ->
                        Pokemon(
                            id = pokemonEntity.id,
                            name = pokemonEntity.name,
                            imageUrl = pokemonEntity.imageUrl,
                            types = pokemonEntity.types
                        )
                    }
                }
                PokemonTeam(id = entity.id, name = entity.name, members = members)
            }
        }

    override suspend fun createTeam(name: String): Long {
        val entity = PokemonTeamEntity(name = name, pokemonIds = emptyList())
        return teamDao.insertTeam(entity)
    }

    override suspend fun renameTeam(id: Long, name: String) {
        val entity = teamDao.getTeamById(id) ?: return
        teamDao.updateTeam(entity.copy(name = name))
    }

    override suspend fun addMember(teamId: Long, pokemonId: Int) {
        val entity = teamDao.getTeamById(teamId) ?: return
        if (entity.pokemonIds.size >= 6) return
        if (pokemonId in entity.pokemonIds) return
        teamDao.updateTeam(entity.copy(pokemonIds = entity.pokemonIds + pokemonId))
    }

    override suspend fun removeMember(teamId: Long, pokemonId: Int) {
        val entity = teamDao.getTeamById(teamId) ?: return
        teamDao.updateTeam(entity.copy(pokemonIds = entity.pokemonIds - pokemonId))
    }

    override suspend fun deleteTeam(id: Long) {
        teamDao.deleteTeam(id)
    }
}
