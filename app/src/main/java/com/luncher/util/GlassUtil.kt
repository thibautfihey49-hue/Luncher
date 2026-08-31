package com.luncher.util
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
object GlassUtil {
    fun bgLiquid(p: SharedPreferences?=null): GradientDrawable {
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#0F1115"), Color.parseColor("#1C1F26"))).apply{ cornerRadius=0f }
    }
    fun liquidCard(p: SharedPreferences?=null)= GradientDrawable().apply{ cornerRadius=28f; setColor(Color.parseColor("#252830")) }
    fun liquidCardSmall(p: SharedPreferences?=null)= GradientDrawable().apply{ cornerRadius=22f; setColor(Color.parseColor("#252830")) }
    fun searchBar(p: SharedPreferences?=null)= GradientDrawable().apply{ cornerRadius=100f; setColor(Color.parseColor("#2A2D36")) }
    fun notifCard(p: SharedPreferences?=null)= GradientDrawable().apply{ cornerRadius=24f; setColor(Color.parseColor("#252830")); setStroke(2, Color.parseColor("#3B82F6")) }
    fun popupCard()= GradientDrawable().apply{ cornerRadius=20f; setColor(Color.parseColor("#1E2028")) }
}
