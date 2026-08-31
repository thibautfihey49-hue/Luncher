package com.luncher.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

data class GlassTheme(val id:String, val name:String, val bg:List<String>, val accent:String, val text:String, val sub:String)

object GlassUtil{
    const val PREF="luncher"
    val themes=listOf(
        GlassTheme("midnight","Midnight Glass", listOf("#0A0A1A","#1A1A3A","#23244F"), "#7C4DFF", "#FFFFFF", "#9AA0C0"),
        GlassTheme("aurora","Aurora", listOf("#0F0C29","#302B63"), "#00E5FF", "#FFFFFF", "#AABBCC"),
        GlassTheme("light","Light Glass", listOf("#F5F7FF","#FFFFFF"), "#6C63FF", "#101010", "#888888")
    )
    fun get(c:Context)=themes[0]
    fun safeColor(h:String, fb:Int=Color.WHITE)=try{Color.parseColor(h)}catch(_:Exception){fb}
    fun bg(t:GlassTheme)=GradientDrawable(GradientDrawable.Orientation.TL_BR, t.bg.map{ safeColor(it)}.toIntArray())
    fun card(corner:Float=28f, bg:String="#14FFFFFF", stroke:String="#1FFFFFFF")=GradientDrawable().apply{ cornerRadius=corner; setColor(safeColor(bg)); setStroke(1, safeColor(stroke))}
    fun cardSolid()=GradientDrawable().apply{ cornerRadius=24f; setColor(Color.parseColor("#15152A")); setStroke(1, Color.parseColor("#23234A"))}
    fun pill(accent:String)=GradientDrawable().apply{ cornerRadius=100f; setColor(safeColor(accent))}
    fun iconBg(colors:List<String>)=GradientDrawable(GradientDrawable.Orientation.TL_BR, colors.map{ safeColor(it)}.toIntArray()).apply{ cornerRadius=28f}
}
