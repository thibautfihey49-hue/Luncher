package com.luncher.util
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object GlassUtil{
    fun bgLiquid(prefs:SharedPreferences?=null): GradientDrawable {
        val theme=prefs?.getString("theme","light")?: "light"
        val colors = when(theme){
            "purple" -> intArrayOf(Color.parseColor("#E8D5FF"), Color.parseColor("#C8B8FF"), Color.parseColor("#8B7CF7"))
            "dark" -> intArrayOf(Color.parseColor("#1A1A2E"), Color.parseColor("#2A2A4A"), Color.parseColor("#3A3A6A"))
            else -> intArrayOf(Color.parseColor("#E8D5FF"), Color.parseColor("#D8D0FF"), Color.parseColor("#B8B8FF"))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
    }
    private fun alphaHex(prefs:SharedPreferences?): String {
        val a = prefs?.getInt("alpha",70)?: 70
        val hex = Integer.toHexString((a*2.55).toInt()).padStart(2,'0')
        return hex
    }
    fun liquidCard(prefs:SharedPreferences?=null) = GradientDrawable().apply{
        cornerRadius=100f
        setColor(Color.parseColor("#${alphaHex(prefs)}FFFFFF"))
        setStroke(1, Color.parseColor("#80FFFFFF"))
    }
    fun liquidCardSmall(prefs:SharedPreferences?=null) = GradientDrawable().apply{
        cornerRadius=26f
        setColor(Color.parseColor("#${alphaHex(prefs)}FFFFFF"))
        setStroke(1, Color.parseColor("#70FFFFFF"))
    }
    fun searchBar(prefs:SharedPreferences?=null) = GradientDrawable().apply{
        cornerRadius=100f
        setColor(Color.parseColor("#E9E9FF"))
        setStroke(1, Color.parseColor("#D6D6F0"))
    }
    fun dock(prefs:SharedPreferences?=null) = GradientDrawable().apply{
        cornerRadius=36f
        setColor(Color.parseColor("#${alphaHex(prefs)}FFFFFF"))
        setStroke(1, Color.parseColor("#90FFFFFF"))
    }
}
