package com.cesar.pokedex.domain.model

data class PokemonTeam(
    val id: Long,
    val name: String,
    val members: List<Pokemon>
)
