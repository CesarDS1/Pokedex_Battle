package com.cesar.pokedex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritePokemonEntity(
    @PrimaryKey val id: Int
)
