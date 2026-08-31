package com.luncher.util
import android.graphics.Color
import android.widget.TextView
import android.view.Gravity

object IconUtil{
    fun style(tv:TextView, emoji:String, colors:List<String>){
        tv.text=emoji; tv.textSize=24f; tv.setTextColor(Color.WHITE)
        tv.background=GlassUtil.iconBg(colors)
        tv.setPadding(0,22,0,22); tv.gravity=Gravity.CENTER
    }
    fun phone(tv:TextView){ style(tv,"📞", listOf("#4CD964","#2FB344"))}
    fun sms(tv:TextView){ style(tv,"💬", listOf("#7C4DFF","#5A8CFF"))}
    fun files(tv:TextView){ style(tv,"📁", listOf("#FFCC00","#FF9500"))}
}
