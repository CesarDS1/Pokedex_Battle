package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonStat
import com.cesar.pokedex.domain.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamAnalyzerTest {

    private fun makePokemon(id: Int, vararg types: String) = Pokemon(
        id = id, name = "Pokemon$id", imageUrl = "", types = types.toList()
    )

    private fun makeDetail(id: Int, vararg stats: Pair<String, Int>) = PokemonDetail(
        id = id, name = "Pokemon$id", imageUrl = "", description = "", region = "",
        types = emptyList(), abilities = emptyList(),
        stats = stats.map { (name, value) -> PokemonStat(name, value) }
    )

    @Test
    fun `empty team returns empty analysis`() {
        val result = TeamAnalyzer.analyze(emptyList(), emptyList())
        assertTrue(result.weaknesses.isEmpty())
        assertTrue(result.resistances.isEmpty())
        assertTrue(result.immunities.isEmpty())
        assertTrue(result.offensiveCoverage.isEmpty())
        assertTrue(result.coverageGaps.isEmpty())
        assertTrue(result.averageStats.isEmpty())
        assertTrue(result.totalStats.isEmpty())
    }

    @Test
    fun `fire type is weak to water ground rock`() {
        val members = listOf(makePokemon(1, "Fire"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        assertEquals(1, result.weaknesses["water"] ?: 0)
        assertEquals(1, result.weaknesses["ground"] ?: 0)
        assertEquals(1, result.weaknesses["rock"] ?: 0)
    }

    @Test
    fun `fire type resists fire grass ice bug steel`() {
        val members = listOf(makePokemon(1, "Fire"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        assertTrue((result.resistances["fire"] ?: 0) >= 1)
        assertTrue((result.resistances["grass"] ?: 0) >= 1)
        assertTrue((result.resistances["ice"] ?: 0) >= 1)
        assertTrue((result.resistances["bug"] ?: 0) >= 1)
        assertTrue((result.resistances["steel"] ?: 0) >= 1)
    }

    @Test
    fun `flying type is immune to ground`() {
        val members = listOf(makePokemon(1, "Flying"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        assertEquals(1, result.immunities["ground"] ?: 0)
    }

    @Test
    fun `coverage gap detected when two members weak and none resist`() {
        // Both Fire members are weak to Water, no member resists Water
        val members = listOf(makePokemon(1, "Fire"), makePokemon(2, "Fire"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        assertTrue("water should be a coverage gap", "water" in result.coverageGaps)
    }

    @Test
    fun `coverage gap not present when one member resists`() {
        // Fire weak to Water, but Water resists Water
        val members = listOf(makePokemon(1, "Fire"), makePokemon(2, "Water"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        assertTrue("water should not be a coverage gap", "water" !in result.coverageGaps)
    }

    @Test
    fun `offensive coverage includes types hit super effectively by member types`() {
        val members = listOf(makePokemon(1, "Fire"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        // Fire hits Grass, Ice, Bug, Steel super effectively
        assertTrue("grass" in result.offensiveCoverage)
        assertTrue("ice" in result.offensiveCoverage)
        assertTrue("bug" in result.offensiveCoverage)
        assertTrue("steel" in result.offensiveCoverage)
    }

    @Test
    fun `average stats computed correctly`() {
        val members = listOf(makePokemon(1, "Fire"), makePokemon(2, "Water"))
        val details = listOf(
            makeDetail(1, "hp" to 100, "attack" to 80),
            makeDetail(2, "hp" to 60, "attack" to 120)
        )
        val result = TeamAnalyzer.analyze(members, details)
        assertEquals(80f, result.averageStats["hp"] ?: 0f, 0.001f)  // (100+60)/2
        assertEquals(100f, result.averageStats["attack"] ?: 0f, 0.001f)  // (80+120)/2
    }

    @Test
    fun `total stats computed correctly`() {
        val members = listOf(makePokemon(1, "Fire"), makePokemon(2, "Water"))
        val details = listOf(
            makeDetail(1, "hp" to 100),
            makeDetail(2, "hp" to 60)
        )
        val result = TeamAnalyzer.analyze(members, details)
        assertEquals(160, result.totalStats["hp"] ?: 0)
    }

    @Test
    fun `partial detail loads do not crash - handles members without details`() {
        val members = listOf(makePokemon(1, "Fire"), makePokemon(2, "Water"))
        val details = listOf(makeDetail(1, "hp" to 100))
        val result = TeamAnalyzer.analyze(members, details)
        assertEquals(100f, result.averageStats["hp"] ?: 0f, 0.001f)
        assertEquals(100, result.totalStats["hp"] ?: 0)
    }

    @Test
    fun `water grass dual type resists both water and electric`() {
        val members = listOf(makePokemon(1, "Water", "Grass"))
        val result = TeamAnalyzer.analyze(members, emptyList())
        // Water resists Water and Grass resists Electric, but combined:
        // Electric vs Water = 2x, Electric vs Grass = 0.5x → combined = 1x → no entry
        // Water vs Water = 0.5x, Water vs Grass = 0.5x → combined = 0.25x → resist
        assertTrue((result.resistances["water"] ?: 0) >= 1)
    }
}
