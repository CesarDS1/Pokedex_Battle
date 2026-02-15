package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocalizedName(
    val name: String,
    val language: NamedApiResource
)
