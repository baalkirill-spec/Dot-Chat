package com.streamgram.core.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class AppLanguage(
    val tag: String,
    val nativeName: String,
) {
    SYSTEM("system", "System"),
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    UKRAINIAN("uk", "Українська"),
    BELARUSIAN("be", "Беларуская"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español"),
    PORTUGUESE("pt", "Português"),
    ITALIAN("it", "Italiano"),
    TURKISH("tr", "Türkçe"),
    ARABIC("ar", "العربية"),
    HINDI("hi", "हिन्दी"),
    INDONESIAN("id", "Bahasa Indonesia"),
    CHINESE("zh", "中文"),
    JAPANESE("ja", "日本語"),
    ;

    val locale: Locale?
        get() = if (this == SYSTEM) null else Locale.forLanguageTag(tag)

    companion object {
        val supported = entries.toList()

        fun fromTag(tag: String?): AppLanguage {
            return entries.firstOrNull { it.tag == tag } ?: SYSTEM
        }
    }
}

fun applyAppLanguage(languageTag: String) {
    val locales = if (languageTag == AppLanguage.SYSTEM.tag) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }
    AppCompatDelegate.setApplicationLocales(locales)
}
