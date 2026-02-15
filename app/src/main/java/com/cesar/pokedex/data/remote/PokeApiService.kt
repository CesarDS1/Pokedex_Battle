package com.cesar.pokedex.data.remote

import com.cesar.pokedex.data.remote.dto.AbilityResponse
import com.cesar.pokedex.data.remote.dto.MoveResponse
import com.cesar.pokedex.data.remote.dto.EvolutionChainResponse
import com.cesar.pokedex.data.remote.dto.StatResponse
import com.cesar.pokedex.data.remote.dto.PokemonDetailResponse
import com.cesar.pokedex.data.remote.dto.PokemonListResponse
import com.cesar.pokedex.data.remote.dto.PokemonSpeciesResponse
import com.cesar.pokedex.data.remote.dto.TypeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    // Fetches a paginated list of all Pokemon species (name and URL only)
    @GET("pokemon-species")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    // Fetches detailed info for a single Pokemon: stats, types, abilities, moves, sprites, and cries
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailResponse

    // Fetches species-level data: flavor text, generation/region, evolution chain URL, and varieties
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesResponse

    // Fetches the list of all types (used to build a Pokemon-to-types mapping for the list screen)
    @GET("type")
    suspend fun getTypeList(
        @Query("limit") limit: Int = 50
    ): PokemonListResponse

    // Fetches a single type's data: damage relations and the list of Pokemon that have this type
    @GET("type/{id}")
    suspend fun getType(@Path("id") id: String): TypeResponse

    // Fetches a single move's data: name and type (used to display level-up moves)
    @GET("move/{name}")
    suspend fun getMove(@Path("name") name: String): MoveResponse

    // Fetches a single ability's data: name and localized names
    @GET("ability/{name}")
    suspend fun getAbility(@Path("name") name: String): AbilityResponse

    // Fetches a single stat's data: name and localized names
    @GET("stat/{name}")
    suspend fun getStat(@Path("name") name: String): StatResponse

    // Fetches an evolution chain: the full tree of species and their evolution triggers
    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") id: Int): EvolutionChainResponse

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
