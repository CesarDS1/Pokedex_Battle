package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.PokemonType

/**
 * Maps a Pokémon's types to their lowercase API slugs (e.g. "fire", "flying"),
 * falling back to the localized display name when apiName is blank.
 * Shared by any screen that needs to feed types into [TypeEffectivenessChart].
 */
fun List<PokemonType>.toTypeKeys(): List<String> = map { it.apiName.ifBlank { it.name.lowercase() } }

val ALL_POKEMON_TYPES =
    listOf(
        "Normal",
        "Fire",
        "Water",
        "Electric",
        "Grass",
        "Ice",
        "Fighting",
        "Poison",
        "Ground",
        "Flying",
        "Psychic",
        "Bug",
        "Rock",
        "Ghost",
        "Dragon",
        "Dark",
        "Steel",
        "Fairy",
    )
