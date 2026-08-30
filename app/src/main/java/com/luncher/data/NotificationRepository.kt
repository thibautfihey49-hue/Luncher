package com.luncher.data

import android.service.notification.StatusBarNotification

object NotificationRepository {
    private val notifs = mutableListOf<StatusBarNotification>()
    fun add(sbn: StatusBarNotification) {
        notifs.removeAll { it.key == sbn.key }
        notifs.add(0, sbn)
    }
    fun remove(key: String) {
        notifs.removeAll { it.key == key }
    }
    fun getAll(): List<StatusBarNotification> = notifs.toList()
}
