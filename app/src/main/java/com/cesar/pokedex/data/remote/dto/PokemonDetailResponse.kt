package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<PokemonTypeSlot>,
    val abilities: List<PokemonAbilitySlot>,
    val moves: List<MoveSlot> = emptyList(),
    val sprites: Sprites,
    val cries: Cries? = null,
    val stats: List<StatSlot> = emptyList()
)

@Serializable
data class StatSlot(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedApiResource
)

@Serializable
data class Cries(
    val latest: String? = null,
    val legacy: String? = null
)

@Serializable
data class PokemonTypeSlot(
    val slot: Int,
    val type: NamedApiResource
)

@Serializable
data class PokemonAbilitySlot(
    val ability: NamedApiResource,
    @SerialName("is_hidden") val isHidden: Boolean
)

@Serializable
data class Sprites(
    @SerialName("front_default") val frontDefault: String? = null,
    val other: OtherSprites? = null
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtwork? = null
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default") val frontDefault: String? = null
)

@Serializable
data class MoveSlot(
    val move: NamedApiResource,
    @SerialName("version_group_details") val versionGroupDetails: List<MoveVersionGroupDetail>
)

@Serializable
data class MoveVersionGroupDetail(
    @SerialName("level_learned_at") val levelLearnedAt: Int,
    @SerialName("move_learn_method") val moveLearnMethod: NamedApiResource,
    @SerialName("version_group") val versionGroup: NamedApiResource
)
