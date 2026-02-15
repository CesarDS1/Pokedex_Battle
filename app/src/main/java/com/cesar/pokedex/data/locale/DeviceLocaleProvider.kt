package com.cesar.pokedex.data.locale

import java.util.Locale
import javax.inject.Inject

class DeviceLocaleProvider @Inject constructor() {

    fun getLanguageCode(): String {
        return when (Locale.getDefault().language) {
            "es" -> "es"
            "fr" -> "fr"
            "de" -> "de"
            "it" -> "it"
            "ja" -> "ja"
            "ko" -> "ko"
            "zh" -> "zh-Hans"
            else -> "en"
        }
    }
}
