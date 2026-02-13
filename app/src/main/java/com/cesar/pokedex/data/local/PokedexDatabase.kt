package com.cesar.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class, PokemonEvolutionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
