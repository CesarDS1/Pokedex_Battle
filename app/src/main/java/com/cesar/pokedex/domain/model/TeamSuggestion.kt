package com.cesar.pokedex.domain.model

data class TeamSuggestion(
    val pokemon: Pokemon,
    val score: Int,
    val coverageDetails: List<String>
)
