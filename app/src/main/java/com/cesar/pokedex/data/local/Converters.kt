package com.cesar.pokedex.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromIntList(value: List<Int>): String = Json.encodeToString(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> = Json.decodeFromString(value)
}
