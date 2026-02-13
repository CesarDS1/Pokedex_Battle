package com.cesar.pokedex.ui.screen.pokemonlist

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerationOfTest {

    @Test
    fun `id 1 is Generation I Kanto`() {
        assertEquals("Generation I — Kanto", generationOf(1))
    }

    @Test
    fun `id 151 is Generation I Kanto`() {
        assertEquals("Generation I — Kanto", generationOf(151))
    }

    @Test
    fun `id 152 is Generation II Johto`() {
        assertEquals("Generation II — Johto", generationOf(152))
    }

    @Test
    fun `id 251 is Generation II Johto`() {
        assertEquals("Generation II — Johto", generationOf(251))
    }

    @Test
    fun `id 252 is Generation III Hoenn`() {
        assertEquals("Generation III — Hoenn", generationOf(252))
    }

    @Test
    fun `id 387 is Generation IV Sinnoh`() {
        assertEquals("Generation IV — Sinnoh", generationOf(387))
    }

    @Test
    fun `id 494 is Generation V Unova`() {
        assertEquals("Generation V — Unova", generationOf(494))
    }

    @Test
    fun `id 650 is Generation VI Kalos`() {
        assertEquals("Generation VI — Kalos", generationOf(650))
    }

    @Test
    fun `id 722 is Generation VII Alola`() {
        assertEquals("Generation VII — Alola", generationOf(722))
    }

    @Test
    fun `id 810 is Generation VIII Galar`() {
        assertEquals("Generation VIII — Galar", generationOf(810))
    }

    @Test
    fun `id 906 is Generation IX Paldea`() {
        assertEquals("Generation IX — Paldea", generationOf(906))
    }

    @Test
    fun `id 1026 is Other`() {
        assertEquals("Other", generationOf(1026))
    }
}
