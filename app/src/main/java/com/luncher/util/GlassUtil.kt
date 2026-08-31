package com.luncher.util
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object GlassUtil{
    fun bgLiquid() = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#E8D5FF"), Color.parseColor("#B8C6FF"), Color.parseColor("#7C5CFF"), Color.parseColor("#4A3A8A"))).apply{ cornerRadius=0f }

    // Liquid Glass Card = blanc transparent + stroke + blur simulé
    fun liquidCard() = GradientDrawable().apply{
        cornerRadius=32f
        setColor(Color.parseColor("#70FFFFFF"))
        setStroke(1, Color.parseColor("#90FFFFFF"))
    }
    fun liquidCardSmall() = GradientDrawable().apply{
        cornerRadius=24f
        setColor(Color.parseColor("#75FFFFFF"))
        setStroke(1, Color.parseColor("#80FFFFFF"))
    }
    fun liquidFolder() = GradientDrawable().apply{
        cornerRadius=40f
        setColor(Color.parseColor("#60FFFFFF"))
        setStroke(1, Color.parseColor("#70FFFFFF"))
    }
    fun dock() = GradientDrawable().apply{
        cornerRadius=40f
        setColor(Color.parseColor("#65FFFFFF"))
        setStroke(1, Color.parseColor("#80FFFFFF"))
    }
    fun colorDot(c:String) = GradientDrawable().apply{ shape=GradientDrawable.OVAL; setColor(Color.parseColor(c)); setStroke(2, Color.parseColor("#60FFFFFF"))}
}
