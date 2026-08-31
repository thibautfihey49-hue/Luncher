package com.luncher.util

import android.content.Context
import android.graphics.Color

object ThemeUtil {
    const val PREFS = "luncher"
    fun getBg(context: Context): Int {
        return try {
            val col = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bg", "#121212") ?: "#121212"
            Color.parseColor(col)
        } catch(_:Exception){ Color.parseColor("#121212") }
    }
    fun saveBg(context: Context, color:String){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bg", color).apply()
    }
    fun log(context: Context, msg:String){
        try{
            val f = java.io.File(context.filesDir, "luncher_debug.txt")
            f.appendText("${System.currentTimeMillis()} $msg\n")
            android.util.Log.e("LUNCHER_DEBUG", msg)
        }catch(_:Exception){}
    }
}
