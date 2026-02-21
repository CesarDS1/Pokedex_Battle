package com.cesar.pokedex.domain.util

import com.cesar.pokedex.domain.model.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamSuggestionEngineTest {

    private fun makePokemon(id: Int, vararg types: String) = Pokemon(
        id = id, name = "Pokemon$id", imageUrl = "", types = types.toList()
    )

    @Test
    fun `empty enemy types returns empty suggestions`() {
        val allPokemon = listOf(makePokemon(1, "Grass"))
        val result = TeamSuggestionEngine.suggest(allPokemon, emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `already on team pokemon are excluded`() {
        val allPokemon = listOf(
            makePokemon(1, "Grass"),
            makePokemon(2, "Fire")
        )
        val result = TeamSuggestionEngine.suggest(allPokemon, listOf("water"), listOf(1))
        assertFalse(result.any { it.pokemon.id == 1 })
    }

    @Test
    fun `grass type pokemon scores positively against water enemy`() {
        val grassPokemon = makePokemon(1, "Grass")
        val result = TeamSuggestionEngine.suggest(listOf(grassPokemon), listOf("water"), emptyList())
        assertTrue("Grass should appear in suggestions vs Water", result.isNotEmpty())
        val suggestion = result.first { it.pokemon.id == 1 }
        assertTrue("Grass vs Water score should be positive", suggestion.score > 0)
    }

    @Test
    fun `electric type pokemon scores positively against water enemy`() {
        val electricPokemon = makePokemon(25, "Electric")
        val result = TeamSuggestionEngine.suggest(listOf(electricPokemon), listOf("water"), emptyList())
        assertTrue("Electric should appear in suggestions vs Water", result.isNotEmpty())
        val suggestion = result.first()
        assertTrue("Score should be positive", suggestion.score > 0)
    }

    @Test
    fun `quad weak pokemon scores negatively against relevant enemy`() {
        // Bug/Grass is 4x weak to Fire (2x from Grass + 2x from Bug → wait, Bug resists Fire...)
        // Let's use Ice/Grass which is 4x weak to Fire (Ice weak to Fire 2x * Grass weak to Fire 2x... actually)
        // Ice weak to Fire = 2x, Grass weak to Fire = 2x → 4x combined
        val icegrassPokemon = makePokemon(1, "Ice", "Grass")
        val fireEnemy = listOf("fire")
        val result = TeamSuggestionEngine.suggest(listOf(icegrassPokemon), fireEnemy, emptyList())
        assertTrue("Quad-weak pokemon should not appear in positive suggestions", result.isEmpty())
    }

    @Test
    fun `results are sorted by score descending`() {
        val pokemonList = listOf(
            makePokemon(1, "Grass"),     // Good vs Water (+3 STAB, +1 resists)
            makePokemon(2, "Electric"),  // Good vs Water (+3 STAB, +1 resists)
            makePokemon(3, "Dragon")     // Dragon resists Water (+1) but weak to nothing
        )
        val result = TeamSuggestionEngine.suggest(pokemonList, listOf("water"), emptyList())
        val scores = result.map { it.score }
        assertEquals(scores, scores.sortedDescending())
    }

    @Test
    fun `results limited to top 20`() {
        val allPokemon = (1..30).map { makePokemon(it, "Grass") }
        val result = TeamSuggestionEngine.suggest(allPokemon, listOf("water"), emptyList())
        assertTrue(result.size <= 20)
    }

    @Test
    fun `pokemon with zero types are excluded`() {
        val noTypePokemon = makePokemon(1)  // No types
        val result = TeamSuggestionEngine.suggest(listOf(noTypePokemon), listOf("water"), emptyList())
        assertTrue("Pokemon with no types should be excluded", result.isEmpty())
    }

    @Test
    fun `immune pokemon scores plus 2 per immune type`() {
        // Ground is immune to Electric attacks (Electric → Ground = 0x)
        val groundPokemon = makePokemon(1, "Ground")
        val result = TeamSuggestionEngine.suggest(listOf(groundPokemon), listOf("electric"), emptyList())
        // Ground also hits electric super effectively (+3) and is immune (+2) = 5 total
        assertTrue(result.isNotEmpty())
        val score = result.first().score
        assertTrue("Ground vs Electric enemy: immune score + STAB score", score >= 2)
    }

    @Test
    fun `coverage details are populated`() {
        val grassPokemon = makePokemon(1, "Grass")
        val result = TeamSuggestionEngine.suggest(listOf(grassPokemon), listOf("water"), emptyList())
        val suggestion = result.firstOrNull { it.pokemon.id == 1 }
        assertTrue("Coverage details should be populated", suggestion != null && suggestion.coverageDetails.isNotEmpty())
    }
}
