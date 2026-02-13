package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)
