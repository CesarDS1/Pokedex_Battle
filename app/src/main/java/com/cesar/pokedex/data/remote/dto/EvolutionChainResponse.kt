package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvolutionChainResponse(
    val chain: ChainLink
)

@Serializable
data class ChainLink(
    val species: NamedApiResource,
    @SerialName("evolves_to") val evolvesTo: List<ChainLink>,
    @SerialName("evolution_details") val evolutionDetails: List<EvolutionDetail>
)

@Serializable
data class EvolutionDetail(
    @SerialName("min_level") val minLevel: Int? = null,
    val trigger: NamedApiResource,
    val item: NamedApiResource? = null
)
