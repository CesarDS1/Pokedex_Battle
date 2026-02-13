package com.cesar.pokedex.data.di

import android.content.Context
import androidx.room.Room
import com.cesar.pokedex.data.local.PokedexDatabase
import com.cesar.pokedex.data.local.dao.PokemonDao
import com.cesar.pokedex.data.remote.PokeApiService
import com.cesar.pokedex.data.repository.PokemonRepositoryImpl
import com.cesar.pokedex.domain.repository.PokemonRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(PokeApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun providePokeApiService(retrofit: Retrofit): PokeApiService =
        retrofit.create(PokeApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokedexDatabase =
        Room.databaseBuilder(context, PokedexDatabase::class.java, "pokedex.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun providePokemonDao(database: PokedexDatabase): PokemonDao = database.pokemonDao()

    @Provides
    @Singleton
    fun providePokemonRepository(api: PokeApiService, dao: PokemonDao): PokemonRepository =
        PokemonRepositoryImpl(api, dao)
}
