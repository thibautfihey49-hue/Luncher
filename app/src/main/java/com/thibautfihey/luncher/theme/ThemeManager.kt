package com.thibautfihey.luncher.theme

import android.content.Context
import org.json.JSONArray

data class LuncherTheme(
    val id: String,
    val name: String,
    val bgColor: String,
    val glassColor: String,
    val textColor: String,
    val desc: String = "",
    val isDark: Boolean = true
)

object ThemeRepository {
    private const val PREFS = "luncher_themes"
    private const val KEY = "selected_theme"
    private const val JSON_PATH = "themes/themes.json"

    fun loadThemes(context: Context): List<LuncherTheme> {
        return try {
            val json = context.assets.open(JSON_PATH).bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LuncherTheme(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    bgColor = o.getString("bg"),
                    glassColor = o.getString("glass"),
                    textColor = o.getString("text"),
                    desc = o.optString("desc", ""),
                    isDark = o.optBoolean("dark", true)
                )
            }
        } catch (e: Exception) {
            // fallback si JSON cassé
            listOf(LuncherTheme("nexus_parallax","NEXUS Parallax 3D Sobre","#0A0E14","#1AFFFFFF","#FFFFFF","",true))
        }
    }

    fun getSelected(context: Context): LuncherTheme {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY, "nexus_parallax") ?: "nexus_parallax"
        return loadThemes(context).find { it.id == id } ?: loadThemes(context).first()
    }

    fun apply(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, themeId).apply()
    }
}
