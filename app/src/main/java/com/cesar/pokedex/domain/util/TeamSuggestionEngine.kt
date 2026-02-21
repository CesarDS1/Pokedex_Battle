package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.TeamSuggestion

object TeamSuggestionEngine {

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

                var score = 0
                val details = mutableListOf<String>()

                for (enemyType in enemyTypesLower) {
                    // Offensive: STAB super effective vs enemy type
                    for (candidateType in candidateTypes) {
                        if (TypeEffectivenessChart.multiplier(candidateType, enemyType) >= 2f) {
                            score += 3
                            details.add("${candidateType.replaceFirstChar { it.uppercase() }} hits $enemyType super effectively")
                        }
                    }

                    // Defensive: how well the candidate handles enemy attacks
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
                        defMult >= 4f -> {
                            score -= 4
                        }
                        defMult >= 2f -> {
                            score -= 2
                        }
                    }
                }

                if (score <= 0) return@mapNotNull null
                TeamSuggestion(pokemon = pokemon, score = score, coverageDetails = details)
            }
            .sortedByDescending { it.score }
            .take(20)
    }
}
