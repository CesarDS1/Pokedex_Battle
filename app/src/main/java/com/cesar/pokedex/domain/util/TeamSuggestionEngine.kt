package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.TeamSuggestion

object TeamSuggestionEngine {

    private fun scoreOnePokemon(
        candidateTypes: List<String>,
        enemyTypesLower: List<String>
    ): Pair<Int, List<String>> {
        var score = 0
        val details = mutableListOf<String>()

        for (enemyType in enemyTypesLower) {
            for (candidateType in candidateTypes) {
                if (TypeEffectivenessChart.multiplier(candidateType, enemyType) >= 2f) {
                    score += 3
                    details.add("${candidateType.replaceFirstChar { it.uppercase() }} hits $enemyType super effectively")
                }
            }
            val defMult = TypeEffectivenessChart.combinedMultiplier(enemyType, candidateTypes)
            when {
                defMult == 0f -> {
                    score += 2
                    details.add("Immune to $enemyType")
                }
                defMult <= 0.5f -> {
                    score += 1
                    details.add("Resists $enemyType")
                }
                defMult >= 4f -> score -= 4
                defMult >= 2f -> score -= 2
            }
        }

        return score to details
    }

    /**
     * Returns the top suggestions (score > 0, capped at 20).
     * Used when showing a short curated list of good counters.
     */
    fun suggest(
        allPokemon: List<Pokemon>,
        enemyTypes: List<String>,
        teamMemberIds: List<Int>
    ): List<TeamSuggestion> {
        if (enemyTypes.isEmpty()) return emptyList()

        val enemyTypesLower = enemyTypes.map { it.lowercase() }
        val memberIdSet = teamMemberIds.toSet()

        return allPokemon
            .filter { it.id !in memberIdSet }
            .mapNotNull { pokemon ->
                val candidateTypes = pokemon.types.map { it.lowercase() }
                if (candidateTypes.isEmpty()) return@mapNotNull null
                val (score, details) = scoreOnePokemon(candidateTypes, enemyTypesLower)
                if (score <= 0) return@mapNotNull null
                TeamSuggestion(pokemon = pokemon, score = score, coverageDetails = details)
            }
            .sortedByDescending { it.score }
            .take(20)
    }

    /**
     * Scores every Pokemon against the given enemy types, including neutral (0)
     * and negative scores. No cap on results. Used to sort and rank the full
     * Pokemon list in AddPokemonToTeamScreen.
     */
    fun scoreAll(
        allPokemon: List<Pokemon>,
        enemyTypes: List<String>,
        teamMemberIds: List<Int>
    ): Map<Int, Int> {
        if (enemyTypes.isEmpty()) return emptyMap()

        val enemyTypesLower = enemyTypes.map { it.lowercase() }
        val memberIdSet = teamMemberIds.toSet()

        return allPokemon
            .filter { it.id !in memberIdSet && it.types.isNotEmpty() }
            .associate { pokemon ->
                val candidateTypes = pokemon.types.map { it.lowercase() }
                val (score, _) = scoreOnePokemon(candidateTypes, enemyTypesLower)
                pokemon.id to score
            }
    }
}
