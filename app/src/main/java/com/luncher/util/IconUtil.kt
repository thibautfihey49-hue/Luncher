package com.luncher.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView

object IconUtil{
    fun styleIcon(tv:TextView, emoji:String, colors:List<String>){
        tv.text=emoji
        tv.textSize=26f
        tv.setTextColor(Color.WHITE)
        tv.background=GlassUtil.iconBg(colors)
        tv.setPadding(0,22,0,22)
        tv.gravity=android.view.Gravity.CENTER
    }
    fun phoneIcon(tv:TextView){ styleIcon(tv,"📞", listOf("#4CAF50","#2E7D32"))}
    fun smsIcon(tv:TextView){ styleIcon(tv,"💬", listOf("#7C4DFF","#00E5FF"))}
    fun filesIcon(tv:TextView){ styleIcon(tv,"📁", listOf("#FFC107","#FF9800"))}
    fun contactIcon(tv:TextView, name:String){
        val cols=listOf(listOf("#7C4DFF","#00E5FF"), listOf("#FF6B6B","#FF8E53"), listOf("#4CAF50","#8BC34A"), listOf("#2196F3","#21CBF3"))
        tv.text=name.firstOrNull()?.uppercase()?:"?"
        tv.textSize=18f
        tv.setTextColor(Color.WHITE)
        tv.background=GlassUtil.iconBg(cols.random())
        tv.gravity=android.view.Gravity.CENTER
    }
}
