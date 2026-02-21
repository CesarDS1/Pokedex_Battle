package com.cesar.pokedex.data.repository

import com.cesar.pokedex.data.locale.DeviceLocaleProvider
import com.cesar.pokedex.data.remote.PokeApiService
import com.cesar.pokedex.data.remote.dto.AbilityResponse
import com.cesar.pokedex.data.remote.dto.ApiResource
import com.cesar.pokedex.data.remote.dto.ChainLink
import com.cesar.pokedex.data.remote.dto.Cries
import com.cesar.pokedex.data.remote.dto.DamageRelations
import com.cesar.pokedex.data.remote.dto.EvolutionChainResponse
import com.cesar.pokedex.data.remote.dto.EvolutionDetail
import com.cesar.pokedex.data.remote.dto.FlavorTextEntry
import com.cesar.pokedex.data.remote.dto.LocalizedName
import com.cesar.pokedex.data.remote.dto.MoveResponse
import com.cesar.pokedex.data.remote.dto.MoveSlot
import com.cesar.pokedex.data.remote.dto.MoveVersionGroupDetail
import com.cesar.pokedex.data.remote.dto.NamedApiResource
import com.cesar.pokedex.data.remote.dto.OfficialArtwork
import com.cesar.pokedex.data.remote.dto.OtherSprites
import com.cesar.pokedex.data.remote.dto.PokemonAbilitySlot
import com.cesar.pokedex.data.remote.dto.PokemonDetailResponse
import com.cesar.pokedex.data.remote.dto.PokemonDto
import com.cesar.pokedex.data.remote.dto.PokemonListResponse
import com.cesar.pokedex.data.remote.dto.PokemonSpeciesResponse
import com.cesar.pokedex.data.remote.dto.PokemonTypeSlot
import com.cesar.pokedex.data.remote.dto.PokemonVarietyEntry
import com.cesar.pokedex.data.remote.dto.Sprites
import com.cesar.pokedex.data.remote.dto.StatResponse
import com.cesar.pokedex.data.remote.dto.TypePokemonSlot
import com.cesar.pokedex.data.remote.dto.TypeResponse
import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity
import com.cesar.pokedex.data.remote.dto.StatSlot
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PokemonRepositoryImplTest {

    private val api: PokeApiService = mockk()
    private val dao: PokemonDao = mockk()
    private val localeProvider: DeviceLocaleProvider = mockk()
    private lateinit var repository: PokemonRepositoryImpl

    // Reusable empty damage relations
    private val emptyDamageRelations = DamageRelations(
        doubleDamageFrom = emptyList(),
        doubleDamageTo = emptyList(),
        halfDamageFrom = emptyList(),
        halfDamageTo = emptyList()
    )

    @Before
    fun setUp() {
        every { localeProvider.getLanguageCode() } returns "en"
        coEvery { dao.getAllPokemon() } returns emptyList()
        coEvery { dao.getPokemonDetail(any()) } returns null
        coEvery { dao.getEvolutionInfo(any()) } returns null
        coJustRun { dao.deleteAllPokemon() }
        coJustRun { dao.insertAllPokemon(any()) }
        coJustRun { dao.insertPokemonDetail(any()) }
        coJustRun { dao.insertEvolutionInfo(any()) }
        repository = PokemonRepositoryImpl(api, dao, localeProvider)
    }

    // region getPokemonList

    @Test
    fun `getPokemonList returns pokemon with correct ids and names`() = runTest {
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 2,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        coEvery { api.getPokemonList(limit = 2) } returns PokemonListResponse(
            count = 2,
            results = listOf(
                PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"),
                PokemonDto("charmander", "https://pokeapi.co/api/v2/pokemon-species/4/")
            )
        )
        stubTypeListEmpty()

        val result = repository.getPokemonList()

        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals("Bulbasaur", result[0].name)
        assertEquals(4, result[1].id)
        assertEquals("Charmander", result[1].name)
    }

    @Test
    fun `getPokemonList builds correct sprite urls`() = runTest {
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"))
        )
        coEvery { api.getPokemonList(limit = 1, offset = 0) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"))
        )
        stubTypeListEmpty()

        val result = repository.getPokemonList()

        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
            result[0].imageUrl
        )
    }

    @Test
    fun `getPokemonList populates types from type map`() = runTest {
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        coEvery { api.getPokemonList(limit = 1, offset = 0) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )

        // Type list returns two types
        coEvery { api.getTypeList(any()) } returns PokemonListResponse(
            count = 2,
            results = listOf(
                PokemonDto("grass", "https://pokeapi.co/api/v2/type/12/"),
                PokemonDto("poison", "https://pokeapi.co/api/v2/type/4/")
            )
        )
        coEvery { api.getType("grass") } returns TypeResponse(
            name = "grass",
            damageRelations = emptyDamageRelations,
            pokemon = listOf(
                TypePokemonSlot(
                    pokemon = NamedApiResource("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                    slot = 1
                )
            ),
            names = listOf(
                LocalizedName("Grass", NamedApiResource("en", ""))
            )
        )
        coEvery { api.getType("poison") } returns TypeResponse(
            name = "poison",
            damageRelations = emptyDamageRelations,
            pokemon = listOf(
                TypePokemonSlot(
                    pokemon = NamedApiResource("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                    slot = 2
                )
            ),
            names = listOf(
                LocalizedName("Poison", NamedApiResource("en", ""))
            )
        )

        val result = repository.getPokemonList()

        assertEquals(listOf("Grass", "Poison"), result[0].types)
    }

    @Test
    fun `getPokemonList returns empty types when type map has no match`() = runTest {
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("missingno", "https://pokeapi.co/api/v2/pokemon-species/999/"))
        )
        coEvery { api.getPokemonList(limit = 1, offset = 0) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("missingno", "https://pokeapi.co/api/v2/pokemon-species/999/"))
        )
        stubTypeListEmpty()

        val result = repository.getPokemonList()

        assertEquals(emptyList<String>(), result[0].types)
    }

    // endregion

    // region getPokemonDetail

    @Test
    fun `getPokemonDetail maps fields correctly`() = runTest {
        stubDetailAndSpecies()

        val result = repository.getPokemonDetail(25)

        assertEquals(25, result.id)
        assertEquals("Pikachu", result.name)
        assertEquals("https://example.com/official/25.png", result.imageUrl)
        assertEquals("Kanto", result.region)
        assertEquals("An electric mouse.", result.description)
    }

    @Test
    fun `getPokemonDetail maps types with damage relations`() = runTest {
        stubDetailAndSpecies()

        val result = repository.getPokemonDetail(25)

        assertEquals(1, result.types.size)
        val type = result.types[0]
        assertEquals("Electric", type.name)
        assertEquals(listOf("Ground"), type.weaknesses)
        assertEquals(listOf("Steel"), type.resistances)
        assertEquals(listOf("Water"), type.strengths)
        assertEquals(listOf("Grass"), type.ineffective)
    }

    @Test
    fun `getPokemonDetail maps abilities correctly`() = runTest {
        stubDetailAndSpecies()

        val result = repository.getPokemonDetail(25)

        assertEquals(2, result.abilities.size)
        assertEquals("Static", result.abilities[0].name)
        assertEquals(false, result.abilities[0].isHidden)
        assertEquals("Lightning Rod", result.abilities[1].name)
        assertEquals(true, result.abilities[1].isHidden)
    }

    @Test
    fun `getPokemonDetail maps cryUrl from latest`() = runTest {
        stubDetailAndSpecies()

        val result = repository.getPokemonDetail(25)

        assertEquals("https://example.com/cries/25.ogg", result.cryUrl)
    }

    @Test
    fun `getPokemonDetail falls back to legacy cry when latest is null`() = runTest {
        stubDetailAndSpecies(
            cries = Cries(latest = null, legacy = "https://example.com/cries/legacy/25.ogg")
        )

        val result = repository.getPokemonDetail(25)

        assertEquals("https://example.com/cries/legacy/25.ogg", result.cryUrl)
    }

    @Test
    fun `getPokemonDetail cryUrl is null when cries is null`() = runTest {
        stubDetailAndSpecies(cries = null)

        val result = repository.getPokemonDetail(25)

        assertEquals(null, result.cryUrl)
    }

    @Test
    fun `getPokemonDetail maps moves sorted by level then name`() = runTest {
        stubDetailAndSpecies(
            moves = listOf(
                MoveSlot(
                    move = NamedApiResource("thunderbolt", "https://pokeapi.co/api/v2/move/85/"),
                    versionGroupDetails = listOf(
                        MoveVersionGroupDetail(
                            levelLearnedAt = 26,
                            moveLearnMethod = NamedApiResource("level-up", ""),
                            versionGroup = NamedApiResource("red-blue", "https://pokeapi.co/api/v2/version-group/1/")
                        )
                    )
                ),
                MoveSlot(
                    move = NamedApiResource("growl", "https://pokeapi.co/api/v2/move/45/"),
                    versionGroupDetails = listOf(
                        MoveVersionGroupDetail(
                            levelLearnedAt = 1,
                            moveLearnMethod = NamedApiResource("level-up", ""),
                            versionGroup = NamedApiResource("red-blue", "https://pokeapi.co/api/v2/version-group/1/")
                        )
                    )
                )
            )
        )
        coEvery { api.getMove("growl") } returns MoveResponse(
            "growl",
            NamedApiResource("normal", ""),
            names = listOf(LocalizedName("Growl", NamedApiResource("en", "")))
        )
        coEvery { api.getMove("thunderbolt") } returns MoveResponse(
            "thunderbolt",
            NamedApiResource("electric", ""),
            names = listOf(LocalizedName("Thunderbolt", NamedApiResource("en", "")))
        )

        val result = repository.getPokemonDetail(25)

        assertEquals(2, result.moves.size)
        assertEquals("Growl", result.moves[0].name)
        assertEquals(1, result.moves[0].level)
        assertEquals("Normal", result.moves[0].type)
        assertEquals("Thunderbolt", result.moves[1].name)
        assertEquals(26, result.moves[1].level)
        assertEquals("Electric", result.moves[1].type)
    }

    @Test
    fun `getPokemonDetail skips non-level-up moves`() = runTest {
        stubDetailAndSpecies(
            moves = listOf(
                MoveSlot(
                    move = NamedApiResource("thunder-wave", "https://pokeapi.co/api/v2/move/86/"),
                    versionGroupDetails = listOf(
                        MoveVersionGroupDetail(
                            levelLearnedAt = 0,
                            moveLearnMethod = NamedApiResource("machine", ""),
                            versionGroup = NamedApiResource("red-blue", "https://pokeapi.co/api/v2/version-group/1/")
                        )
                    )
                )
            )
        )

        val result = repository.getPokemonDetail(25)

        assertTrue(result.moves.isEmpty())
    }

    @Test
    fun `getPokemonDetail falls back to front_default sprite`() = runTest {
        stubDetailAndSpecies(
            sprites = Sprites(frontDefault = "https://example.com/front/25.png", other = null)
        )

        val result = repository.getPokemonDetail(25)

        assertEquals("https://example.com/front/25.png", result.imageUrl)
    }

    @Test
    fun `getPokemonDetail returns empty imageUrl when no sprites`() = runTest {
        stubDetailAndSpecies(
            sprites = Sprites(frontDefault = null, other = null)
        )

        val result = repository.getPokemonDetail(25)

        assertEquals("", result.imageUrl)
    }

    @Test
    fun `getPokemonDetail maps all generation regions`() = runTest {
        val generationMap = mapOf(
            "generation-i" to "Kanto",
            "generation-ii" to "Johto",
            "generation-iii" to "Hoenn",
            "generation-iv" to "Sinnoh",
            "generation-v" to "Unova",
            "generation-vi" to "Kalos",
            "generation-vii" to "Alola",
            "generation-viii" to "Galar",
            "generation-ix" to "Paldea",
            "generation-x" to "Unknown"
        )

        for ((gen, expectedRegion) in generationMap) {
            stubDetailAndSpecies(generation = gen)
            val result = repository.getPokemonDetail(25)
            assertEquals("Generation $gen should map to $expectedRegion", expectedRegion, result.region)
        }
    }

    @Test
    fun `getPokemonDetail returns empty description when no english entry`() = runTest {
        stubDetailAndSpecies(
            flavorTextEntries = listOf(
                FlavorTextEntry("Un ratón eléctrico.", NamedApiResource("es", ""))
            )
        )

        val result = repository.getPokemonDetail(25)

        assertEquals("", result.description)
    }

    // endregion

    // region getEvolutionInfo

    @Test
    fun `getEvolutionInfo returns evolution chain`() = runTest {
        // flattenChain fetches species for each stage, so we stub all of them.
        // The root species (id=1) is fetched first in fetchAndCacheEvolutionInfo,
        // so it needs evolutionChain and varieties too.
        stubEvolutionSpecies(2, "Ivysaur")
        stubEvolutionSpecies(3, "Venusaur")
        coEvery { api.getPokemonSpecies(1) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = ApiResource("https://pokeapi.co/api/v2/evolution-chain/1/"),
            varieties = listOf(
                PokemonVarietyEntry(
                    isDefault = true,
                    pokemon = NamedApiResource("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
                )
            ),
            names = listOf(LocalizedName("Bulbasaur", NamedApiResource("en", "")))
        )
        coEvery { api.getEvolutionChain(1) } returns EvolutionChainResponse(
            chain = ChainLink(
                species = NamedApiResource("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"),
                evolvesTo = listOf(
                    ChainLink(
                        species = NamedApiResource("ivysaur", "https://pokeapi.co/api/v2/pokemon-species/2/"),
                        evolvesTo = listOf(
                            ChainLink(
                                species = NamedApiResource("venusaur", "https://pokeapi.co/api/v2/pokemon-species/3/"),
                                evolvesTo = emptyList(),
                                evolutionDetails = listOf(
                                    EvolutionDetail(
                                        minLevel = 32,
                                        trigger = NamedApiResource("level-up", "")
                                    )
                                )
                            )
                        ),
                        evolutionDetails = listOf(
                            EvolutionDetail(
                                minLevel = 16,
                                trigger = NamedApiResource("level-up", "")
                            )
                        )
                    )
                ),
                evolutionDetails = emptyList()
            )
        )

        val result = repository.getEvolutionInfo(1)

        assertEquals(3, result.evolutions.size)
        assertEquals("Bulbasaur", result.evolutions[0].name)
        assertEquals("Base", result.evolutions[0].trigger)
        assertEquals("Ivysaur", result.evolutions[1].name)
        assertEquals("Level 16", result.evolutions[1].trigger)
        assertEquals("Venusaur", result.evolutions[2].name)
        assertEquals("Level 32", result.evolutions[2].trigger)
    }

    @Test
    fun `getEvolutionInfo returns varieties`() = runTest {
        coEvery { api.getPokemonSpecies(6) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = null,
            varieties = listOf(
                PokemonVarietyEntry(
                    isDefault = true,
                    pokemon = NamedApiResource("charizard", "https://pokeapi.co/api/v2/pokemon/6/")
                ),
                PokemonVarietyEntry(
                    isDefault = false,
                    pokemon = NamedApiResource("charizard-mega-x", "https://pokeapi.co/api/v2/pokemon/10034/")
                )
            )
        )

        val result = repository.getEvolutionInfo(6)

        assertEquals(2, result.varieties.size)
        assertEquals("Charizard", result.varieties[0].name)
        assertEquals(true, result.varieties[0].isDefault)
        assertEquals("Charizard Mega X", result.varieties[1].name)
        assertEquals(false, result.varieties[1].isDefault)
    }

    @Test
    fun `getEvolutionInfo returns empty evolutions when no chain`() = runTest {
        coEvery { api.getPokemonSpecies(1) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = null,
            varieties = listOf(
                PokemonVarietyEntry(
                    isDefault = true,
                    pokemon = NamedApiResource("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
                )
            )
        )

        val result = repository.getEvolutionInfo(1)

        assertTrue(result.evolutions.isEmpty())
    }

    @Test
    fun `getEvolutionInfo formats trade trigger`() = runTest {
        stubEvolutionSpecies(67, "Machoke")
        stubEvolutionSpecies(68, "Machamp")
        coEvery { api.getPokemonSpecies(66) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = ApiResource("https://pokeapi.co/api/v2/evolution-chain/33/"),
            varieties = listOf(
                PokemonVarietyEntry(true, NamedApiResource("machop", "https://pokeapi.co/api/v2/pokemon/66/"))
            ),
            names = listOf(LocalizedName("Machop", NamedApiResource("en", "")))
        )
        coEvery { api.getEvolutionChain(33) } returns EvolutionChainResponse(
            chain = ChainLink(
                species = NamedApiResource("machop", "https://pokeapi.co/api/v2/pokemon-species/66/"),
                evolvesTo = listOf(
                    ChainLink(
                        species = NamedApiResource("machoke", "https://pokeapi.co/api/v2/pokemon-species/67/"),
                        evolvesTo = listOf(
                            ChainLink(
                                species = NamedApiResource("machamp", "https://pokeapi.co/api/v2/pokemon-species/68/"),
                                evolvesTo = emptyList(),
                                evolutionDetails = listOf(
                                    EvolutionDetail(trigger = NamedApiResource("trade", ""))
                                )
                            )
                        ),
                        evolutionDetails = listOf(
                            EvolutionDetail(
                                minLevel = 28,
                                trigger = NamedApiResource("level-up", "")
                            )
                        )
                    )
                ),
                evolutionDetails = emptyList()
            )
        )

        val result = repository.getEvolutionInfo(66)

        assertEquals("Trade", result.evolutions[2].trigger)
    }

    @Test
    fun `getEvolutionInfo formats use-item trigger`() = runTest {
        stubEvolutionSpecies(134, "Vaporeon")
        coEvery { api.getPokemonSpecies(133) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = ApiResource("https://pokeapi.co/api/v2/evolution-chain/67/"),
            varieties = listOf(
                PokemonVarietyEntry(true, NamedApiResource("eevee", "https://pokeapi.co/api/v2/pokemon/133/"))
            ),
            names = listOf(LocalizedName("Eevee", NamedApiResource("en", "")))
        )
        coEvery { api.getEvolutionChain(67) } returns EvolutionChainResponse(
            chain = ChainLink(
                species = NamedApiResource("eevee", "https://pokeapi.co/api/v2/pokemon-species/133/"),
                evolvesTo = listOf(
                    ChainLink(
                        species = NamedApiResource("vaporeon", "https://pokeapi.co/api/v2/pokemon-species/134/"),
                        evolvesTo = emptyList(),
                        evolutionDetails = listOf(
                            EvolutionDetail(
                                trigger = NamedApiResource("use-item", ""),
                                item = NamedApiResource("water-stone", "")
                            )
                        )
                    )
                ),
                evolutionDetails = emptyList()
            )
        )

        val result = repository.getEvolutionInfo(133)

        assertEquals("Use Water stone", result.evolutions[1].trigger)
    }

    @Test
    fun `getEvolutionInfo formats level-up without min level`() = runTest {
        stubEvolutionSpecies(196, "Espeon")
        coEvery { api.getPokemonSpecies(133) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = ApiResource("https://pokeapi.co/api/v2/evolution-chain/67/"),
            varieties = listOf(
                PokemonVarietyEntry(true, NamedApiResource("eevee", "https://pokeapi.co/api/v2/pokemon/133/"))
            ),
            names = listOf(LocalizedName("Eevee", NamedApiResource("en", "")))
        )
        coEvery { api.getEvolutionChain(67) } returns EvolutionChainResponse(
            chain = ChainLink(
                species = NamedApiResource("eevee", "https://pokeapi.co/api/v2/pokemon-species/133/"),
                evolvesTo = listOf(
                    ChainLink(
                        species = NamedApiResource("espeon", "https://pokeapi.co/api/v2/pokemon-species/196/"),
                        evolvesTo = emptyList(),
                        evolutionDetails = listOf(
                            EvolutionDetail(
                                minLevel = null,
                                trigger = NamedApiResource("level-up", "")
                            )
                        )
                    )
                ),
                evolutionDetails = emptyList()
            )
        )

        val result = repository.getEvolutionInfo(133)

        assertEquals("Level up", result.evolutions[1].trigger)
    }

    @Test
    fun `getPokemonDetail maps stats correctly`() = runTest {
        stubDetailAndSpecies(
            stats = listOf(
                StatSlot(baseStat = 35, stat = NamedApiResource("hp", "")),
                StatSlot(baseStat = 55, stat = NamedApiResource("attack", "")),
                StatSlot(baseStat = 90, stat = NamedApiResource("special-attack", ""))
            )
        )
        coEvery { api.getStat("hp") } returns StatResponse(
            "hp",
            names = listOf(LocalizedName("HP", NamedApiResource("en", "")))
        )
        coEvery { api.getStat("attack") } returns StatResponse(
            "attack",
            names = listOf(LocalizedName("Attack", NamedApiResource("en", "")))
        )
        coEvery { api.getStat("special-attack") } returns StatResponse(
            "special-attack",
            names = listOf(LocalizedName("Special Attack", NamedApiResource("en", "")))
        )

        val result = repository.getPokemonDetail(25)

        assertEquals(3, result.stats.size)
        assertEquals("HP", result.stats[0].name)
        assertEquals(35, result.stats[0].baseStat)
        assertEquals("Attack", result.stats[1].name)
        assertEquals(55, result.stats[1].baseStat)
        assertEquals("Special Attack", result.stats[2].name)
        assertEquals(90, result.stats[2].baseStat)
    }

    @Test
    fun `getPokemonDetail returns cached data when available`() = runTest {
        val cachedJson = """{"id":25,"name":"Pikachu","imageUrl":"https://example.com/25.png","description":"Cached.","region":"Kanto","types":[],"abilities":[]}"""
        coEvery { dao.getPokemonDetail(25) } returns PokemonDetailEntity(id = 25, json = cachedJson)

        val result = repository.getPokemonDetail(25)

        assertEquals(25, result.id)
        assertEquals("Pikachu", result.name)
        assertEquals("Cached.", result.description)
        coVerify(exactly = 0) { api.getPokemonDetail(any()) }
    }

    @Test
    fun `getPokemonList returns cached data when available`() = runTest {
        coEvery { dao.getAllPokemon() } returns listOf(
            PokemonEntity(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass"))
        )

        val result = repository.getPokemonList()

        assertEquals(1, result.size)
        assertEquals("Bulbasaur", result[0].name)
        assertEquals(listOf("Grass"), result[0].types)
        coVerify(exactly = 0) { api.getPokemonList(any()) }
    }

    @Test
    fun `getPokemonList with forceRefresh skips cache`() = runTest {
        coEvery { dao.getAllPokemon() } returns listOf(
            PokemonEntity(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Grass"))
        )
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        coEvery { api.getPokemonList(limit = 1, offset = 0) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        stubTypeListEmpty()

        val result = repository.getPokemonList(forceRefresh = true)

        assertEquals(1, result.size)
        coVerify { dao.deleteAllPokemon() }
        coVerify { api.getPokemonList(limit = 1) }
    }

    @Test
    fun `getPokemonList re-fetches when cached pokemon have empty types`() = runTest {
        coEvery { dao.getAllPokemon() } returns listOf(
            PokemonEntity(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = emptyList())
        )
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        stubTypeListEmpty()

        repository.getPokemonList()

        coVerify { dao.deleteAllPokemon() }
        coVerify { api.getPokemonList(limit = 1) }
    }

    @Test
    fun `getPokemonList re-fetches when cached types are not English`() = runTest {
        // "Fuego" is Spanish for Fire and is NOT in the English type name set
        coEvery { dao.getAllPokemon() } returns listOf(
            PokemonEntity(id = 1, name = "Bulbasaur", imageUrl = "https://example.com/1.png", types = listOf("Fuego"))
        )
        coEvery { api.getPokemonList(limit = 1) } returns PokemonListResponse(
            count = 1,
            results = listOf(PokemonDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"))
        )
        stubTypeListEmpty()

        repository.getPokemonList()

        coVerify { dao.deleteAllPokemon() }
        coVerify { api.getPokemonList(limit = 1) }
    }

    @Test
    fun `getEvolutionInfo returns cached data when available`() = runTest {
        val cachedJson = """{"evolutions":[],"varieties":[{"id":1,"name":"Bulbasaur","imageUrl":"https://example.com/1.png","isDefault":true}]}"""
        coEvery { dao.getEvolutionInfo(1) } returns PokemonEvolutionEntity(id = 1, json = cachedJson)

        val result = repository.getEvolutionInfo(1)

        assertEquals(1, result.varieties.size)
        assertEquals("Bulbasaur", result.varieties[0].name)
        coVerify(exactly = 0) { api.getPokemonSpecies(any()) }
    }

    // endregion

    // region Helpers

    private fun stubTypeListEmpty() {
        coEvery { api.getTypeList(any()) } returns PokemonListResponse(
            count = 0,
            results = emptyList()
        )
    }

    private fun stubEvolutionSpecies(id: Int, englishName: String) {
        coEvery { api.getPokemonSpecies(id) } returns PokemonSpeciesResponse(
            flavorTextEntries = emptyList(),
            generation = NamedApiResource("generation-i", ""),
            evolutionChain = null,
            varieties = emptyList(),
            names = listOf(LocalizedName(englishName, NamedApiResource("en", "")))
        )
    }

    private fun stubDetailAndSpecies(
        cries: Cries? = Cries(latest = "https://example.com/cries/25.ogg"),
        moves: List<MoveSlot> = emptyList(),
        sprites: Sprites = Sprites(
            frontDefault = "https://example.com/front/25.png",
            other = OtherSprites(officialArtwork = OfficialArtwork("https://example.com/official/25.png"))
        ),
        generation: String = "generation-i",
        flavorTextEntries: List<FlavorTextEntry> = listOf(
            FlavorTextEntry("An electric mouse.", NamedApiResource("en", ""))
        ),
        stats: List<StatSlot> = emptyList()
    ) {
        coEvery { api.getPokemonDetail(25) } returns PokemonDetailResponse(
            id = 25,
            name = "pikachu",
            species = NamedApiResource("pikachu", "https://pokeapi.co/api/v2/pokemon-species/25/"),
            height = 4,
            weight = 60,
            types = listOf(
                PokemonTypeSlot(1, NamedApiResource("electric", ""))
            ),
            abilities = listOf(
                PokemonAbilitySlot(NamedApiResource("static", ""), isHidden = false),
                PokemonAbilitySlot(NamedApiResource("lightning-rod", ""), isHidden = true)
            ),
            moves = moves,
            sprites = sprites,
            cries = cries,
            stats = stats
        )
        coEvery { api.getPokemonSpecies(25) } returns PokemonSpeciesResponse(
            flavorTextEntries = flavorTextEntries,
            generation = NamedApiResource(generation, ""),
            evolutionChain = null,
            varieties = emptyList(),
            names = listOf(LocalizedName("Pikachu", NamedApiResource("en", "")))
        )
        coEvery { api.getType("electric") } returns TypeResponse(
            name = "electric",
            damageRelations = DamageRelations(
                doubleDamageFrom = listOf(NamedApiResource("ground", "")),
                doubleDamageTo = listOf(NamedApiResource("water", "")),
                halfDamageFrom = listOf(NamedApiResource("steel", "")),
                halfDamageTo = listOf(NamedApiResource("grass", ""))
            ),
            names = listOf(LocalizedName("Electric", NamedApiResource("en", "")))
        )
        coEvery { api.getAbility("static") } returns AbilityResponse(
            "static",
            names = listOf(LocalizedName("Static", NamedApiResource("en", "")))
        )
        coEvery { api.getAbility("lightning-rod") } returns AbilityResponse(
            "lightning-rod",
            names = listOf(LocalizedName("Lightning Rod", NamedApiResource("en", "")))
        )
        // Default empty stats stubs
        if (stats.isEmpty()) return
        for (stat in stats) {
            coEvery { api.getStat(stat.stat.name) } returns StatResponse(
                stat.stat.name,
                names = listOf(
                    LocalizedName(
                        stat.stat.name.replace("-", " ").split(" ").joinToString(" ") {
                            it.replaceFirstChar { c -> c.uppercase() }
                        },
                        NamedApiResource("en", "")
                    )
                )
            )
        }
    }

    // endregion
}
