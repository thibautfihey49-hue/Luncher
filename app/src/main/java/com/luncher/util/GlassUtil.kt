package com.luncher.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

data class GlassTheme(val id:String, val name:String, val bg:List<String>, val accent:String, val text:String, val subText:String)

object GlassUtil{
    const val PREF="luncher"
    val themes=listOf(
        GlassTheme("glass","Light Glass", listOf("#F5F7FF","#FFFFFF"), "#6C63FF", "#101010", "#888888"),
        GlassTheme("midnight","Midnight Pro", listOf("#0A0A14","#1A1A3A","#23234D"), "#7C4DFF", "#FFFFFF", "#9AA0C0"),
        GlassTheme("aurora","Aurora", listOf("#0F0C29","#302B63"), "#00E5FF", "#FFFFFF", "#AABBCC"),
        GlassTheme("amoled","AMOLED", listOf("#000000","#111111"), "#FFFFFF", "#FFFFFF", "#777777")
    )
    fun get(c:Context):GlassTheme{ val id=c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString("theme","midnight")!!; return themes.find{it.id==id}?:themes[1]}
    fun save(c:Context, t:GlassTheme){ c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString("theme",t.id).apply()}
    fun safeColor(hex:String, fallback:Int=Color.WHITE):Int = try{ Color.parseColor(hex)}catch(_:Exception){ fallback}
    fun bg(t:GlassTheme)=try{ GradientDrawable(GradientDrawable.Orientation.TL_BR, t.bg.map{ safeColor(it)}.toIntArray())}catch(_:Exception){ GradientDrawable().apply{ setColor(Color.parseColor("#0A0A14"))}}
    fun card(corner:Float=28f, bgAlpha:String="#14FFFFFF", stroke:String="#1FFFFFFF")=GradientDrawable().apply{
        try{ cornerRadius=corner; setColor(safeColor(bgAlpha)); setStroke(2, safeColor(stroke))}catch(_:Exception){ setColor(Color.parseColor("#141414"))}
    }
    fun cardSolid()=GradientDrawable().apply{ cornerRadius=24f; setColor(Color.parseColor("#1C1C2E")); setStroke(1, Color.parseColor("#2A2A45"))}
    fun pill(accent:String)=GradientDrawable().apply{ cornerRadius=100f; setColor(safeColor(accent))}
}
