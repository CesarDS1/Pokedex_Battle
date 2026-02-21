package com.cesar.pokedex.domain.model

data class TeamAnalysis(
    val weaknesses: Map<String, Int>,
    val resistances: Map<String, Int>,
    val immunities: Map<String, Int>,
    val offensiveCoverage: Set<String>,
    val averageStats: Map<String, Float>,
    val totalStats: Map<String, Int>,
    val coverageGaps: List<String>
)
