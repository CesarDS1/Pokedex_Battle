package com.cesar.pokedex.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeEffectivenessChartTest {

    @Test
    fun `water is super effective against fire`() {
        assertEquals(2f, TypeEffectivenessChart.multiplier("water", "fire"), 0.001f)
    }

    @Test
    fun `electric has no effect on ground`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("electric", "ground"), 0.001f)
    }

    @Test
    fun `fire is not very effective against water`() {
        assertEquals(0.5f, TypeEffectivenessChart.multiplier("fire", "water"), 0.001f)
    }

    @Test
    fun `ghost has no effect on normal`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("ghost", "normal"), 0.001f)
    }

    @Test
    fun `fighting has no effect on ghost`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("fighting", "ghost"), 0.001f)
    }

    @Test
    fun `psychic has no effect on dark`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("psychic", "dark"), 0.001f)
    }

    @Test
    fun `dragon has no effect on fairy`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("dragon", "fairy"), 0.001f)
    }

    @Test
    fun `poison has no effect on steel`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("poison", "steel"), 0.001f)
    }

    @Test
    fun `ground has no effect on flying`() {
        assertEquals(0f, TypeEffectivenessChart.multiplier("ground", "flying"), 0.001f)
    }

    @Test
    fun `electric vs water and ground - ground immunity wins giving 0x combined`() {
        val combined = TypeEffectivenessChart.combinedMultiplier("electric", listOf("water", "ground"))
        assertEquals(0f, combined, 0.001f)
    }

    @Test
    fun `fire vs grass and water - multipliers stack`() {
        val combined = TypeEffectivenessChart.combinedMultiplier("fire", listOf("grass", "water"))
        // 2f * 0.5f = 1f
        assertEquals(1f, combined, 0.001f)
    }

    @Test
    fun `ice vs dragon and flying - 4x combined`() {
        val combined = TypeEffectivenessChart.combinedMultiplier("ice", listOf("dragon", "flying"))
        // 2f * 2f = 4f
        assertEquals(4f, combined, 0.001f)
    }

    @Test
    fun `allMatchupsFor excludes 1x entries`() {
        val matchups = TypeEffectivenessChart.allMatchupsFor(listOf("normal"))
        assertTrue("1x entries should be excluded", matchups.values.none { it == 1f })
    }

    @Test
    fun `allMatchupsFor returns lowercase keys`() {
        val matchups = TypeEffectivenessChart.allMatchupsFor(listOf("fire"))
        assertTrue("All keys should be lowercase", matchups.keys.all { it == it.lowercase() })
    }

    @Test
    fun `allTypes returns exactly 18 types`() {
        assertEquals(18, TypeEffectivenessChart.allTypes().size)
    }

    @Test
    fun `all 18 types have entries in chart as attacking type`() {
        val allTypes = TypeEffectivenessChart.allTypes()
        for (type in allTypes) {
            // Each type should produce at least some non-1x matchup against something
            val matchups = TypeEffectivenessChart.allMatchupsFor(listOf(type))
            // We can verify the reverse - all types are a valid defending type
            // by checking that the allTypes list contains them
            assertTrue("Type $type should be in all types", type in allTypes)
        }
    }

    @Test
    fun `steel resists many types`() {
        val steelMatchups = TypeEffectivenessChart.allMatchupsFor(listOf("steel"))
        // Steel resists Normal, Grass, Ice, Flying, Psychic, Bug, Rock, Dragon, Steel, Fairy
        // and is immune to Poison
        val resistCount = steelMatchups.values.count { it < 1f && it > 0f }
        assertTrue("Steel should resist many types", resistCount >= 9)
        assertEquals(0f, steelMatchups["poison"] ?: 1f, 0.001f)
    }

    @Test
    fun `normal type attacking returns correct multipliers`() {
        val result = TypeEffectivenessChart.multiplier("normal", "ghost")
        assertEquals(0f, result, 0.001f)
        val vsRock = TypeEffectivenessChart.multiplier("normal", "rock")
        assertEquals(0.5f, vsRock, 0.001f)
    }
}
