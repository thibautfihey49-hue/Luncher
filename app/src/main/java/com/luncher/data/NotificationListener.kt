package com.luncher.data

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class LuncherNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { NotificationRepository.add(it) }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { NotificationRepository.remove(it.key) }
    }
}
