package com.cesar.pokedex.data.repository

import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity
import com.cesar.pokedex.data.remote.PokeApiService
import com.cesar.pokedex.data.remote.dto.ChainLink
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.Move
import com.cesar.pokedex.domain.model.PokemonStat
import com.cesar.pokedex.domain.model.EvolutionStage
import com.cesar.pokedex.domain.model.Pokemon
import com.cesar.pokedex.domain.model.PokemonDetail
import com.cesar.pokedex.domain.model.PokemonEvolutionInfo
import com.cesar.pokedex.domain.model.PokemonType
import com.cesar.pokedex.domain.model.PokemonVariety
import com.cesar.pokedex.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val dao: PokemonDao
) : PokemonRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPokemonList(forceRefresh: Boolean): List<Pokemon> {
        if (!forceRefresh) {
            val cached = dao.getAllPokemon()
            if (cached.isNotEmpty()) {
                return cached.map { it.toDomain() }
            }
        }
        dao.deleteAllPokemon()
        return fetchAndCachePokemonList()
    }

    private suspend fun fetchAndCachePokemonList(): List<Pokemon> = coroutineScope {
        val countResponse = api.getPokemonList(limit = 1)
        val responseDeferred = async { api.getPokemonList(limit = countResponse.count) }
        val typeMapDeferred = async { buildTypeMap() }

        val response = responseDeferred.await()
        val typeMap = typeMapDeferred.await()

        val pokemonList = response.results.map { dto ->
            val id = dto.url.trimEnd('/').substringAfterLast('/').toInt()
            Pokemon(
                id = id,
                name = dto.name.replaceFirstChar { it.uppercase() },
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
                types = typeMap[id]?.map { it.replaceFirstChar { c -> c.uppercase() } } ?: emptyList()
            )
        }

        dao.insertAllPokemon(pokemonList.map { it.toEntity() })
        pokemonList
    }

    private suspend fun buildTypeMap(): Map<Int, List<String>> = coroutineScope {
        val typeList = api.getTypeList()
        val typeResponses = typeList.results.map { dto ->
            async { api.getType(dto.name) }
        }.awaitAll()

        val typeMap = mutableMapOf<Int, MutableList<Pair<Int, String>>>()
        for (typeResponse in typeResponses) {
            for (slot in typeResponse.pokemon) {
                val pokemonId = slot.pokemon.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: continue
                typeMap.getOrPut(pokemonId) { mutableListOf() }.add(slot.slot to typeResponse.name)
            }
        }
        typeMap.mapValues { (_, slots) -> slots.sortedBy { it.first }.map { it.second } }
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val cached = dao.getPokemonDetail(id)
        if (cached != null) {
            return json.decodeFromString<PokemonDetail>(cached.json)
        }
        return fetchAndCachePokemonDetail(id)
    }

    private suspend fun fetchAndCachePokemonDetail(id: Int): PokemonDetail = coroutineScope {
        val detailDeferred = async { api.getPokemonDetail(id) }
        val speciesDeferred = async { api.getPokemonSpecies(id) }

        val detail = detailDeferred.await()
        val species = speciesDeferred.await()

        val typeResponses = detail.types.map { slot ->
            async { api.getType(slot.type.name) }
        }.awaitAll()

        val description = species.flavorTextEntries
            .firstOrNull { it.language.name == "en" }
            ?.flavorText
            ?.replace("\n", " ")
            ?.replace("\u000c", " ")
            ?.replace("  ", " ")
            ?.trim()
            ?: ""

        val region = when (species.generation.name) {
            "generation-i" -> "Kanto"
            "generation-ii" -> "Johto"
            "generation-iii" -> "Hoenn"
            "generation-iv" -> "Sinnoh"
            "generation-v" -> "Unova"
            "generation-vi" -> "Kalos"
            "generation-vii" -> "Alola"
            "generation-viii" -> "Galar"
            "generation-ix" -> "Paldea"
            else -> "Unknown"
        }

        val imageUrl = detail.sprites.other?.officialArtwork?.frontDefault
            ?: detail.sprites.frontDefault
            ?: ""

        val types = typeResponses.map { typeResponse ->
            PokemonType(
                name = typeResponse.name.replaceFirstChar { it.uppercase() },
                weaknesses = typeResponse.damageRelations.doubleDamageFrom.map {
                    it.name.replaceFirstChar { c -> c.uppercase() }
                },
                resistances = typeResponse.damageRelations.halfDamageFrom.map {
                    it.name.replaceFirstChar { c -> c.uppercase() }
                },
                strengths = typeResponse.damageRelations.doubleDamageTo.map {
                    it.name.replaceFirstChar { c -> c.uppercase() }
                },
                ineffective = typeResponse.damageRelations.halfDamageTo.map {
                    it.name.replaceFirstChar { c -> c.uppercase() }
                }
            )
        }

        val abilities = detail.abilities.map { slot ->
            Ability(
                name = slot.ability.name.replaceFirstChar { it.uppercase() }.replace("-", " "),
                isHidden = slot.isHidden
            )
        }

        val levelUpSlots = detail.moves.mapNotNull { slot ->
            val levelUpDetails = slot.versionGroupDetails
                .filter { it.moveLearnMethod.name == "level-up" }
            if (levelUpDetails.isEmpty()) return@mapNotNull null
            val latest = levelUpDetails.maxBy { it.versionGroup.url.trimEnd('/').substringAfterLast('/').toInt() }
            slot.move.name to latest.levelLearnedAt
        }

        val moveResponses = levelUpSlots.map { (moveName, _) ->
            async { api.getMove(moveName) }
        }.awaitAll()

        val moves = levelUpSlots.zip(moveResponses) { (moveName, level), moveResponse ->
            Move(
                name = moveName.replace("-", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                },
                level = level,
                type = moveResponse.type.name.replaceFirstChar { it.uppercase() }
            )
        }.sortedWith(compareBy<Move> { it.level }.thenBy { it.name })

        val stats = detail.stats.map { slot ->
            PokemonStat(
                name = slot.stat.name.replace("-", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                },
                baseStat = slot.baseStat
            )
        }

        val pokemonDetail = PokemonDetail(
            id = detail.id,
            name = detail.name.replaceFirstChar { it.uppercase() },
            imageUrl = imageUrl,
            description = description,
            region = region,
            types = types,
            abilities = abilities,
            moves = moves,
            cryUrl = detail.cries?.latest ?: detail.cries?.legacy,
            stats = stats
        )

        dao.insertPokemonDetail(PokemonDetailEntity(id = id, json = json.encodeToString(pokemonDetail)))
        pokemonDetail
    }

    override suspend fun getEvolutionInfo(id: Int): PokemonEvolutionInfo {
        val cached = dao.getEvolutionInfo(id)
        if (cached != null) {
            return json.decodeFromString<PokemonEvolutionInfo>(cached.json)
        }
        return fetchAndCacheEvolutionInfo(id)
    }

    private suspend fun fetchAndCacheEvolutionInfo(id: Int): PokemonEvolutionInfo {
        val species = api.getPokemonSpecies(id)

        val evolutions = mutableListOf<EvolutionStage>()
        val chainUrl = species.evolutionChain?.url
        if (chainUrl != null) {
            val chainId = chainUrl.trimEnd('/').substringAfterLast('/').toInt()
            val chain = api.getEvolutionChain(chainId)
            flattenChain(chain.chain, evolutions)
        }

        val varieties = species.varieties
            .map { entry ->
                val varId = entry.pokemon.url.trimEnd('/').substringAfterLast('/').toInt()
                PokemonVariety(
                    id = varId,
                    name = formatVarietyName(entry.pokemon.name),
                    imageUrl = spriteUrl(varId),
                    isDefault = entry.isDefault
                )
            }

        val info = PokemonEvolutionInfo(
            evolutions = evolutions,
            varieties = varieties
        )

        dao.insertEvolutionInfo(PokemonEvolutionEntity(id = id, json = json.encodeToString(info)))
        return info
    }

    private fun flattenChain(link: ChainLink, result: MutableList<EvolutionStage>) {
        val speciesId = link.species.url.trimEnd('/').substringAfterLast('/').toInt()
        val trigger = if (link.evolutionDetails.isEmpty()) {
            "Base"
        } else {
            formatTrigger(link.evolutionDetails.first())
        }
        result.add(
            EvolutionStage(
                id = speciesId,
                name = link.species.name.replaceFirstChar { it.uppercase() },
                imageUrl = spriteUrl(speciesId),
                trigger = trigger
            )
        )
        for (next in link.evolvesTo) {
            flattenChain(next, result)
        }
    }

    private fun formatTrigger(detail: com.cesar.pokedex.data.remote.dto.EvolutionDetail): String {
        return when (detail.trigger.name) {
            "level-up" -> {
                if (detail.minLevel != null) "Level ${detail.minLevel}"
                else "Level up"
            }
            "use-item" -> {
                val itemName = detail.item?.name
                    ?.replace("-", " ")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "item"
                "Use $itemName"
            }
            "trade" -> "Trade"
            else -> detail.trigger.name.replace("-", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatVarietyName(raw: String): String {
        return raw.replace("-", " ").split(" ").joinToString(" ") {
            it.replaceFirstChar { c -> c.uppercase() }
        }
    }

    private fun spriteUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    private fun PokemonEntity.toDomain() = Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types
    )

    private fun Pokemon.toEntity() = PokemonEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types
    )
}
