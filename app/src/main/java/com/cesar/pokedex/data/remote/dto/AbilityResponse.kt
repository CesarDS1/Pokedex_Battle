package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AbilityResponse(
    val name: String,
    val names: List<LocalizedName> = emptyList()
)
