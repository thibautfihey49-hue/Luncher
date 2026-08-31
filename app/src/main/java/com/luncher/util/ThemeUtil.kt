package com.luncher.util

import android.content.Context
import android.graphics.Color

object ThemeUtil {
    const val PREFS = "luncher"
    fun getBg(context: Context): Int {
        return try {
            val col = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bg", "#F5F5F7") ?: "#F5F5F7"
            Color.parseColor(col)
        } catch(_:Exception){ Color.parseColor("#F5F5F7") }
    }
    fun isDark(bg: Int): Boolean {
        val darkness = 1 - (0.299*Color.red(bg) + 0.587*Color.green(bg) + 0.114*Color.blue(bg))/255
        return darkness >= 0.5
    }
    fun getTextColor(bg: Int): Int = if(isDark(bg)) Color.WHITE else Color.parseColor("#1A1A1A")
    fun saveBg(context: Context, color:String){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bg", color).apply()
    }
}
