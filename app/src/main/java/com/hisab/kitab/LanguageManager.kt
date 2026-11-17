
package com.hisab.kitab

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    private const val PREFS = "hisab_language"
    private const val KEY_LANGUAGE = "selected_language"

    fun getLanguage(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "hi")
            ?.takeIf { it == "hi" || it == "en" }
            ?: "hi"
    }

    fun setLanguage(context: Context, language: String) {
        val safeLanguage = if (language == "en") "en" else "hi"

        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, safeLanguage)
            .apply()
    }

    fun applyLanguage(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale.forLanguageTag(language)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        ).apply {
            setLocale(locale)
        }

        return context.createConfigurationContext(configuration)
    }
}
