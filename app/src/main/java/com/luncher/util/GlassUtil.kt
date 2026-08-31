package com.luncher.util
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

object GlassUtil {
    fun bgLiquid(prefs: SharedPreferences? = null): Drawable {
        val theme = prefs?.getString("theme", "dark")?: "dark"
        val colors = when (theme) {
            "black" -> intArrayOf(Color.parseColor("#000000"), Color.parseColor("#111111"))
            "dark" -> intArrayOf(Color.parseColor("#0F1115"), Color.parseColor("#1C1F26"), Color.parseColor("#2A2D3A"))
            "blue" -> intArrayOf(Color.parseColor("#0A1628"), Color.parseColor("#12233F"), Color.parseColor("#1A3A5F"))
            "grey" -> intArrayOf(Color.parseColor("#1A1A1A"), Color.parseColor("#2E2E2E"))
            "light" -> intArrayOf(Color.parseColor("#EDEEF2"), Color.parseColor("#D8D9E0"))
            else -> intArrayOf(Color.parseColor("#0F1115"), Color.parseColor("#2A2D3A"))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
    }
    private fun alphaHex(prefs: SharedPreferences?): String {
        val a = prefs?.getInt("alpha", 85)?: 85
        return Integer.toHexString((a * 2.55).toInt()).padStart(2, '0')
    }
    fun liquidCard(prefs: SharedPreferences? = null) = GradientDrawable().apply {
        cornerRadius = 28f
        setColor(Color.parseColor("#${alphaHex(prefs)}1E2028"))
        setStroke(1, Color.parseColor("#30FFFFFF"))
    }
    fun liquidCardSmall(prefs: SharedPreferences? = null) = GradientDrawable().apply {
        cornerRadius = 22f
        setColor(Color.parseColor("#${alphaHex(prefs)}252830"))
        setStroke(1, Color.parseColor("#25FFFFFF"))
    }
    fun searchBar(prefs: SharedPreferences? = null) = GradientDrawable().apply {
        cornerRadius = 100f
        setColor(Color.parseColor("#2A2D36"))
        setStroke(1, Color.parseColor("#3A3D4A"))
    }
    fun notifCard(prefs: SharedPreferences? = null) = GradientDrawable().apply {
        cornerRadius = 24f
        setColor(Color.parseColor("#${alphaHex(prefs)}252830"))
        setStroke(1, Color.parseColor("#3B82F6"))
    }
    fun popupCard() = GradientDrawable().apply{
        cornerRadius = 20f
        setColor(Color.parseColor("#1E2028"))
        setStroke(1, Color.parseColor("#50FFFFFF"))
    }
}
