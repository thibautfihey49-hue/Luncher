package com.luncher.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

data class GlassTheme(val id:String, val name:String, val bg:List<String>, val accent:String, val text:String)

object GlassUtil{
    const val PREF="luncher"
    val themes=listOf(
        GlassTheme("glass","Glass Light", listOf("#E8EAF6","#F5F5F7"), "#6A5ACD", "#1A1A1A"),
        GlassTheme("dark","Glass Dark", listOf("#0A0A0A","#1A1A2E"), "#BB86FC", "#FFFFFF"),
        GlassTheme("aurora","Aurora", listOf("#0F0C29","#302B63","#24243E"), "#00DBDE", "#FFFFFF"),
        GlassTheme("sunset","Sunset", listOf("#FF512F","#DD2476"), "#FFE53B", "#FFFFFF")
    )
    fun get(c:Context):GlassTheme{ val id=c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString("theme","glass")!!; return themes.find{it.id==id}?:themes[0]}
    fun save(c:Context, t:GlassTheme){ c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString("theme",t.id).apply()}
    fun bg(t:GlassTheme)=GradientDrawable(GradientDrawable.Orientation.TL_BR, t.bg.map{Color.parseColor(it)}.toIntArray())
    fun glassCard(corner:Float=28f):GradientDrawable = GradientDrawable().apply{
        cornerRadius=corner; setColor(Color.parseColor("#0DFFFFFF")); setStroke(2, Color.parseColor("#1AFFFFFF"))
    }
    fun glassCardSolid(accent:String, corner:Float=24f)=GradientDrawable().apply{
        cornerRadius=corner; setColor(Color.parseColor("#121212")); setStroke(1, Color.parseColor(accent))
    }
}
