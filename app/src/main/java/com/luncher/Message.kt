package com.luncher

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@kotlinx.parcelize.Parcelize
data class Message(
    val id: String,
    val type: String, // "SMS", "WHATSAPP", "GMAIL"
    val sender: String,
    val content: String,
    val time: Long,
    val packageName: String,
    val isRead: Boolean = false
) : Parcelable
