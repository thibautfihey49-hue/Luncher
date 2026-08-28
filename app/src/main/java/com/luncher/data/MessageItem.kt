package com.luncher.data

data class MessageItem(
    val id: Long,
    val appName: String,
    val packageName: String,
    val sender: String,
    val content: String,
    val time: String,
    val icon: Int
)
