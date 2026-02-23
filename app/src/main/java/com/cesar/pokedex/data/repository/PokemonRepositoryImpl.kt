package com.cesar.pokedex.data.repository

import com.cesar.pokedex.data.locale.DeviceLocaleProvider
import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.local.entity.FavoritePokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonDetailEntity
import com.cesar.pokedex.data.local.entity.PokemonEntity
import com.cesar.pokedex.data.local.entity.PokemonEvolutionEntity
import com.cesar.pokedex.data.remote.PokeApiService
import com.cesar.pokedex.data.remote.dto.ChainLink
import com.cesar.pokedex.data.remote.dto.LocalizedName
import com.cesar.pokedex.domain.model.Ability
import com.cesar.pokedex.domain.model.GameEntry
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val dao: PokemonDao,
    private val localeProvider: DeviceLocaleProvider
) : PokemonRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // Used to detect stale cache entries that were stored with localized (non-English) type names
    // before the fix that switched buildTypeMap() to always use the PokeAPI English slug.
    private val englishTypeNames = setOf(
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
        "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
        "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    )

    override suspend fun getPokemonList(forceRefresh: Boolean): List<Pokemon> {
        if (!forceRefresh) {
            val cached = dao.getAllPokemon()
            val firstType = cached.firstOrNull()?.types?.firstOrNull()
            if (firstType != null && firstType in englishTypeNames) {
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
                types = typeMap[id] ?: emptyList()
            )
        }

        dao.insertAllPokemon(pokemonList.map { it.toEntity() })
        pokemonList
    }

    // Type names are stored in English so that typeColor(), TypeEffectivenessChart, and the type
    // filter all work correctly regardless of the device locale. Localized names are only needed
    // for the detail screen, which fetches them separately via getPokemonDetail().
    private suspend fun buildTypeMap(): Map<Int, List<String>> = coroutineScope {
        val typeList = api.getTypeList()
        val typeResponses = typeList.results.map { dto ->
            async { api.getType(dto.name) }
        }.awaitAll()

        val typeMap = mutableMapOf<Int, MutableList<Pair<Int, String>>>()
        for (typeResponse in typeResponses) {
            val typeName = typeResponse.name.replaceFirstChar { it.uppercase() }
            for (slot in typeResponse.pokemon) {
                val pokemonId = slot.pokemon.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: continue
                typeMap.getOrPut(pokemonId) { mutableListOf() }.add(slot.slot to typeName)
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
        val lang = localeProvider.getLanguageCode()
        val detail = api.getPokemonDetail(id)
        val speciesId = detail.species.url.trimEnd('/').substringAfterLast('/').toInt()
        val species = api.getPokemonSpecies(speciesId)

        val typeResponses = detail.types.map { slot ->
            async { api.getType(slot.type.name) }
        }.awaitAll()

        val description = species.flavorTextEntries
            .firstOrNull { it.language.name == lang }
            ?.flavorText
            ?: species.flavorTextEntries
                .firstOrNull { it.language.name == "en" }
                ?.flavorText
                ?: ""
        val cleanDescription = description
            .replace("\n", " ")
            .replace("\u000c", " ")
            .replace("  ", " ")
            .trim()

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
            val localizedTypeName = typeResponse.names.localized(lang)
                ?: typeResponse.name.replaceFirstChar { it.uppercase() }
            PokemonType(
                name = localizedTypeName.replaceFirstChar { it.uppercase() },
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

        val abilityResponses = detail.abilities.map { slot ->
            async { api.getAbility(slot.ability.name) }
        }.awaitAll()

        val abilities = detail.abilities.zip(abilityResponses) { slot, abilityResponse ->
            val localizedName = abilityResponse.names.localized(lang)
                ?: slot.ability.name.replaceFirstChar { it.uppercase() }.replace("-", " ")
            Ability(
                name = localizedName,
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

        val moves = levelUpSlots.zip(moveResponses) { (_, level), moveResponse ->
            val localizedMoveName = moveResponse.names.localized(lang)
                ?: moveResponse.name.replace("-", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                }
            val flavorText = moveResponse.flavorTextEntries
                .filter { it.language.name == lang }
                .maxByOrNull { it.versionGroup.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0 }
                ?.flavorText
                ?: moveResponse.flavorTextEntries
                    .filter { it.language.name == "en" }
                    .maxByOrNull { it.versionGroup.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0 }
                    ?.flavorText
                ?: ""
            Move(
                name = localizedMoveName,
                level = level,
                type = moveResponse.type.name.replaceFirstChar { it.uppercase() },
                description = flavorText.replace("\n", " ").replace("\\s+".toRegex(), " ").trim()
            )
        }.sortedWith(compareBy<Move> { it.level }.thenBy { it.name })

        val statResponses = detail.stats.map { slot ->
            async { api.getStat(slot.stat.name) }
        }.awaitAll()

        val stats = detail.stats.zip(statResponses) { slot, statResponse ->
            val localizedStatName = statResponse.names.localized(lang)
                ?: slot.stat.name.replace("-", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                }
            PokemonStat(
                name = localizedStatName,
                baseStat = slot.baseStat
            )
        }

        val localizedPokemonName = species.names.localized(lang)
            ?: detail.name.replaceFirstChar { it.uppercase() }

        val gameEntries = species.flavorTextEntries
            .distinctBy { it.version.name }
            .map { entry ->
                GameEntry(
                    gameName = entry.version.name
                        .replace("-", " ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                )
            }

        val pokemonDetail = PokemonDetail(
            id = speciesId,
            name = localizedPokemonName,
            imageUrl = imageUrl,
            description = cleanDescription,
            region = region,
            heightDecimeters = detail.height,
            weightHectograms = detail.weight,
            genderRate = species.genderRate,
            types = types,
            abilities = abilities,
            moves = moves,
            cryUrl = detail.cries?.latest ?: detail.cries?.legacy,
            stats = stats,
            gameEntries = gameEntries
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

    private suspend fun fetchAndCacheEvolutionInfo(id: Int): PokemonEvolutionInfo = coroutineScope {
        val lang = localeProvider.getLanguageCode()
        val species = api.getPokemonSpecies(id)

        val evolutions = mutableListOf<EvolutionStage>()
        val chainUrl = species.evolutionChain?.url
        if (chainUrl != null) {
            val chainId = chainUrl.trimEnd('/').substringAfterLast('/').toInt()
            val chain = api.getEvolutionChain(chainId)
            flattenChain(chain.chain, evolutions, lang)
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
        info
    }

    private suspend fun flattenChain(
        link: ChainLink,
        result: MutableList<EvolutionStage>,
        lang: String
    ) {
        val speciesId = link.species.url.trimEnd('/').substringAfterLast('/').toInt()
        val trigger = if (link.evolutionDetails.isEmpty()) {
            "Base"
        } else {
            formatTrigger(link.evolutionDetails.first())
        }

        val speciesResponse = api.getPokemonSpecies(speciesId)
        val localizedName = speciesResponse.names.localized(lang)
            ?: link.species.name.replaceFirstChar { it.uppercase() }

        result.add(
            EvolutionStage(
                id = speciesId,
                name = localizedName,
                imageUrl = spriteUrl(speciesId),
                trigger = trigger
            )
        )
        for (next in link.evolvesTo) {
            flattenChain(next, result, lang)
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

    override fun getFavoriteIds(): Flow<Set<Int>> =
        dao.getFavoriteIds().map { it.toSet() }

    override suspend fun toggleFavorite(pokemonId: Int) {
        if (dao.isFavorite(pokemonId)) {
            dao.deleteFavorite(pokemonId)
        } else {
            dao.insertFavorite(FavoritePokemonEntity(pokemonId))
        }
    }

    private fun spriteUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    private fun List<LocalizedName>.localized(lang: String): String? =
        firstOrNull { it.language.name == lang }?.name
            ?: firstOrNull { it.language.name == "en" }?.name

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
