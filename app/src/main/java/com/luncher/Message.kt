package com.luncher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: String,
    val type: String,
    val sender: String,
    val content: String,
    val time: Long,
    val packageName: String,
    val isRead: Boolean = false
) : Parcelable
