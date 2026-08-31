package com.luncher.util
import android.content.Context
import android.graphics.Color
object ThemeUtil{
    const val PREFS="luncher"
    fun getBg(c:Context):Int = try{ Color.parseColor(c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("bg","#F5F5F7")!!)}catch(_:Exception){ Color.parseColor("#F5F5F7")}
    fun isDark(bg:Int):Boolean = (1-(0.299*Color.red(bg)+0.587*Color.green(bg)+0.114*Color.blue(bg))/255) >= 0.5
    fun getTextColor(bg:Int):Int = if(isDark(bg)) Color.WHITE else Color.parseColor("#1A1A1A")
    fun getHintColor(bg:Int):Int = if(isDark(bg)) Color.parseColor("#AAAAAA") else Color.parseColor("#888888")
    fun saveBg(c:Context, col:String){ c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("bg",col).apply()}
    fun log(c:Context, msg:String){ try{ java.io.File(c.filesDir,"luncher_debug.txt").appendText("$msg\n")}catch(_:Exception){} }
}
