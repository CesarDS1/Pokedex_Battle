package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.TeamAnalysis

object TeamAnalyzer {

    fun analyze(members: List<Pokemon>, memberDetails: List<PokemonDetail>): TeamAnalysis {
        val allTypes = TypeEffectivenessChart.allTypes()

        val weaknesses = mutableMapOf<String, Int>()
        val resistances = mutableMapOf<String, Int>()
        val immunities = mutableMapOf<String, Int>()

        for (member in members) {
            val defTypes = member.types.map { it.lowercase() }
            if (defTypes.isEmpty()) continue
            for (attackingType in allTypes) {
                val mult = TypeEffectivenessChart.combinedMultiplier(attackingType, defTypes)
                when {
                    mult == 0f -> immunities[attackingType] = (immunities[attackingType] ?: 0) + 1
                    mult < 1f -> resistances[attackingType] = (resistances[attackingType] ?: 0) + 1
                    mult > 1f -> weaknesses[attackingType] = (weaknesses[attackingType] ?: 0) + 1
                }
            }
        }

        // Offensive coverage: set of types that at least one team member's type hits super effectively
        val offensiveCoverage = mutableSetOf<String>()
        for (member in members) {
            for (memberType in member.types.map { it.lowercase() }) {
                for (targetType in allTypes) {
                    if (TypeEffectivenessChart.multiplier(memberType, targetType) >= 2f) {
                        offensiveCoverage.add(targetType)
                    }
                }
            }
        }

        // Coverage gaps: types where >=2 members are weak AND 0 members resist/immune
        val coverageGaps = allTypes.filter { attackingType ->
            val weakCount = weaknesses[attackingType] ?: 0
            val resistCount = resistances[attackingType] ?: 0
            val immuneCount = immunities[attackingType] ?: 0
            weakCount >= 2 && resistCount == 0 && immuneCount == 0
        }

        // Stats (from loaded member details only)
        val statTotals = mutableMapOf<String, Int>()
        val statCounts = mutableMapOf<String, Int>()
        for (detail in memberDetails) {
            for (stat in detail.stats) {
                statTotals[stat.name] = (statTotals[stat.name] ?: 0) + stat.baseStat
                statCounts[stat.name] = (statCounts[stat.name] ?: 0) + 1
            }
        }
        val totalStats = statTotals.toMap()
        val averageStats = statTotals.mapValues { (name, total) ->
            total.toFloat() / (statCounts[name] ?: 1)
        }

        return TeamAnalysis(
            weaknesses = weaknesses,
            resistances = resistances,
            immunities = immunities,
            offensiveCoverage = offensiveCoverage,
            averageStats = averageStats,
            totalStats = totalStats,
            coverageGaps = coverageGaps
        )
    }
}
