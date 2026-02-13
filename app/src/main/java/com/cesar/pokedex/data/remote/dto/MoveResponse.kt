package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoveResponse(
    val name: String,
    val type: NamedApiResource
)
