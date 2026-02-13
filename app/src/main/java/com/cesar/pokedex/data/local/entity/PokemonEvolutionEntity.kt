package com.cesar.pokedex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_evolution")
data class PokemonEvolutionEntity(
    @PrimaryKey val id: Int,
    val json: String
)
