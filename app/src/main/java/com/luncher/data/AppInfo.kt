package com.luncher.data; import android.graphics.drawable.Drawable
data class AppInfo(val name: String, val packageName: String, val icon: Drawable)
data class MessageItem(val id: Long=System.currentTimeMillis(), val appName: String, val packageName: String, val sender: String, val content: String, val time: String, val icon: Int)
