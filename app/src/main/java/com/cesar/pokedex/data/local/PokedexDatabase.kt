package com.cesar.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.dao.TeamDao
import com.cesar.pokedex.data.local.entity.FavoritePokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity
import com.cesar.pokedex.data.local.entity.PokemonTeamEntity

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class, PokemonEvolutionEntity::class, FavoritePokemonEntity::class, PokemonTeamEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun teamDao(): TeamDao
}
