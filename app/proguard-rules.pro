# Preserve stack traces in production
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room ─────────────────────────────────────────────────────────────────────
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database class *
-keep @androidx.room.TypeConverters class *

# ── kotlinx.serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.cesar.pokedex.**$$serializer { *; }
-keepclassmembers class com.cesar.pokedex.** {
    *** Companion;
}
-keepclasseswithmembers class com.cesar.pokedex.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Retrofit ─────────────────────────────────────────────────────────────────
-keep interface com.cesar.pokedex.data.remote.PokeApiService { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── OkHttp ───────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep @dagger.hilt.InstallIn class *
-keep @dagger.Module class *
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# ── Coil ─────────────────────────────────────────────────────────────────────
-dontwarn coil.**
