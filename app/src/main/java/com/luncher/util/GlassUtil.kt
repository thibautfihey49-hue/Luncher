package com.luncher.util
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object GlassUtil{
    fun safe(c:String, fb:Int=Color.WHITE)=try{Color.parseColor(c)}catch(_:Exception){fb}
    // LIGHT GLASS
    fun bg() = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#F8F9FF"), Color.parseColor("#FFFFFF"), Color.parseColor("#EEF2FF")))

    fun searchBg() = GradientDrawable().apply{
        cornerRadius=100f
        setColor(Color.parseColor("#F1F3F9"))
        setStroke(1, Color.parseColor("#E6E8F0"))
    }
    fun card() = GradientDrawable().apply{
        cornerRadius=32f
        setColor(Color.WHITE)
        setStroke(1, Color.parseColor("#EDEFF5"))
    }
    fun cardShadow() = card()
    fun dock() = GradientDrawable().apply{
        cornerRadius=44f
        setColor(Color.parseColor("#F2F4FA"))
        setStroke(1, Color.parseColor("#E6E8F0"))
    }
    fun pill(c:String) = GradientDrawable().apply{ cornerRadius=100f; setColor(safe(c))}
    fun iconBg(c:List<String>) = GradientDrawable(GradientDrawable.Orientation.TL_BR, c.map{ safe(it)}.toIntArray()).apply{ cornerRadius=28f}
}
