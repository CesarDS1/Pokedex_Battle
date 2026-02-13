package com.cesar.pokedex.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonEvolutionInfo(
    val evolutions: List<EvolutionStage>,
    val varieties: List<PokemonVariety>
)

@Serializable
data class EvolutionStage(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val trigger: String
)

@Serializable
data class PokemonVariety(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isDefault: Boolean
)
