package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoveResponse(
    val name: String,
    val type: NamedApiResource,
    val names: List<LocalizedName> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<MoveFlavorTextEntry> = emptyList()
)

@Serializable
data class MoveFlavorTextEntry(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResource,
    @SerialName("version_group") val versionGroup: NamedApiResource
)
