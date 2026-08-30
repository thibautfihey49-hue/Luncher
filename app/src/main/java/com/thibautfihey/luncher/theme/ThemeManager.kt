package com.thibautfihey.luncher.theme

import android.content.Context
import android.content.SharedPreferences

data class LuncherTheme(
    val id: String,
    val name: String,
    val bgColor: String,
    val glassColor: String,
    val textColor: String,
    val isDark: Boolean
)

object ThemeRepository {
    private const val PREFS = "luncher_themes"
    private const val KEY = "selected_theme"

    val themes = listOf(
        LuncherTheme("nexus_parallax", "NEXUS Parallax 3D Sobre", "#0A0E14", "#1AFFFFFF", "#FFFFFF", true),
        LuncherTheme("glass_blue", "Modern Glass 3D Bleu", "#FFFFFF", "#E3F2FD", "#1E293B", false),
        LuncherTheme("frost_white", "Frost White Minimal", "#F8FAFC", "#FFFFFF", "#0F172A", false),
        LuncherTheme("midnight", "Midnight Black Pro", "#000000", "#111827", "#F9FAFB", true)
    )

    fun getSelected(context: Context): LuncherTheme {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY, "nexus_parallax")?: "nexus_parallax"
        return themes.find { it.id == id }?: themes[0]
    }

    fun apply(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
           .edit().putString(KEY, themeId).apply()
    }
}
