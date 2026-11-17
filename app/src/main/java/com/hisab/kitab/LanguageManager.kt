
package com.hisab.kitab

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    private const val PREFS = "hisab_language"
    private const val KEY_LANGUAGE = "selected_language"

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(KEY_LANGUAGE, "hi") ?: "hi"
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun applyLanguage(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale.forLanguageTag(language)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}
