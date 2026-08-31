package com.luncher.util
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object GlassUtil{
    fun bgLiquid() = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#E8D5FF"), Color.parseColor("#C8B8FF"), Color.parseColor("#8B7CF7"))).apply{ cornerRadius=0f }
    fun liquidCard() = GradientDrawable().apply{ cornerRadius=32f; setColor(Color.parseColor("#88FFFFFF")); setStroke(1, Color.parseColor("#A0FFFFFF"))}
    fun liquidCardSmall() = GradientDrawable().apply{ cornerRadius=26f; setColor(Color.parseColor("#90FFFFFF")); setStroke(1, Color.parseColor("#80FFFFFF"))}
    fun liquidFolder() = GradientDrawable().apply{ cornerRadius=32f; setColor(Color.parseColor("#70FFFFFF")); setStroke(1, Color.parseColor("#80FFFFFF"))}
    fun dock() = GradientDrawable().apply{ cornerRadius=36f; setColor(Color.parseColor("#75FFFFFF")); setStroke(1, Color.parseColor("#90FFFFFF"))}
    fun searchBar() = GradientDrawable().apply{ cornerRadius=100f; setColor(Color.parseColor("#E9E9FF")); setStroke(1, Color.parseColor("#D6D6F0"))}
    fun colorDot(c:String) = GradientDrawable().apply{ shape=GradientDrawable.OVAL; setColor(Color.parseColor(c)); setStroke(2, Color.parseColor("#60FFFFFF"))}
    fun pill() = GradientDrawable().apply{ shape=GradientDrawable.OVAL; setColor(Color.BLACK); cornerRadius=100f}
}
