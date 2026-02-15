package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonSpeciesResponse(
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    val generation: NamedApiResource,
    @SerialName("evolution_chain") val evolutionChain: ApiResource?,
    val varieties: List<PokemonVarietyEntry>,
    @SerialName("gender_rate") val genderRate: Int = -1,
    val names: List<LocalizedName> = emptyList()
)

@Serializable
data class FlavorTextEntry(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResource
)

@Serializable
data class ApiResource(
    val url: String
)

@Serializable
data class PokemonVarietyEntry(
    @SerialName("is_default") val isDefault: Boolean,
    val pokemon: NamedApiResource
)
