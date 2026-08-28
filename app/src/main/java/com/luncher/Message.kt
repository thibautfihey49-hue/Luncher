package com.luncher

data class Message(
    val id: String,
    val type: String,
    val sender: String,
    val content: String,
    val time: Long,
    val packageName: String
)
