package com.cesar.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TypeResponse(
    val name: String,
    @SerialName("damage_relations") val damageRelations: DamageRelations,
    val pokemon: List<TypePokemonSlot> = emptyList(),
    val names: List<LocalizedName> = emptyList()
)

@Serializable
data class TypePokemonSlot(
    val pokemon: NamedApiResource,
    val slot: Int
)

@Serializable
data class DamageRelations(
    @SerialName("double_damage_from") val doubleDamageFrom: List<NamedApiResource>,
    @SerialName("double_damage_to") val doubleDamageTo: List<NamedApiResource>,
    @SerialName("half_damage_from") val halfDamageFrom: List<NamedApiResource>,
    @SerialName("half_damage_to") val halfDamageTo: List<NamedApiResource>
)
