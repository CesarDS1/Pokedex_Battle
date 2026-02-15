package com.cesar.pokedex.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val region: String,
    val heightDecimeters: Int = 0,
    val weightHectograms: Int = 0,
    val genderRate: Int = -1,
    val types: List<PokemonType>,
    val abilities: List<Ability>,
    val moves: List<Move> = emptyList(),
    val cryUrl: String? = null,
    val stats: List<PokemonStat> = emptyList()
)

@Serializable
data class PokemonType(
    val name: String,
    val weaknesses: List<String>,
    val resistances: List<String>,
    val strengths: List<String>,
    val ineffective: List<String>
)

@Serializable
data class Ability(
    val name: String,
    val isHidden: Boolean
)

@Serializable
data class PokemonStat(
    val name: String,
    val baseStat: Int
)

@Serializable
data class Move(
    val name: String,
    val level: Int,
    val type: String
)
