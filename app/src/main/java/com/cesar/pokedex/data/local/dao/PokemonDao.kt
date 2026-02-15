package com.cesar.pokedex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cesar.pokedex.data.local.entity.FavoritePokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    suspend fun getAllPokemon(): List<PokemonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPokemon(pokemon: List<PokemonEntity>)

    @Query("DELETE FROM pokemon")
    suspend fun deleteAllPokemon()

    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    suspend fun getPokemonDetail(id: Int): PokemonDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonDetail(detail: PokemonDetailEntity)

    @Query("SELECT * FROM pokemon_evolution WHERE id = :id")
    suspend fun getEvolutionInfo(id: Int): PokemonEvolutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvolutionInfo(evolution: PokemonEvolutionEntity)

    @Query("SELECT id FROM favorites")
    fun getFavoriteIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePokemonEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean
}
