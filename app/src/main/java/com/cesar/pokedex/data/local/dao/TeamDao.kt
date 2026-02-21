package com.cesar.pokedex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cesar.pokedex.data.local.entity.PokemonTeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

    @Query("SELECT * FROM teams ORDER BY id ASC")
    fun getAllTeams(): Flow<List<PokemonTeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: Long): PokemonTeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: PokemonTeamEntity): Long

    @Update
    suspend fun updateTeam(team: PokemonTeamEntity)

    @Query("DELETE FROM teams WHERE id = :id")
    suspend fun deleteTeam(id: Long)
}
