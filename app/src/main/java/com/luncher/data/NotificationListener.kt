
package com.luncher.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

object NotificationRepository {
    data class FloatingNotif(
        val id: Int,
        val packageName: String,
        val appName: String,
        val title: String,
        val content: String,
        val time: Long,
        val notification: Notification,
        val sbnKey: String
    )
    val notifs = mutableListOf<FloatingNotif>()
    var listener: (() -> Unit)? = null
}

class NotificationListener : NotificationListenerService() {

    companion object {
        private var _instance: NotificationListener? = null
        @JvmStatic
        fun getInstance(): NotificationListener? = _instance
    }

    // TES 3 APPS EXACTES DES SCREENS
    private val allowedPkgs = setOf(
        "com.whatsapp",
        "com.google.android.apps.messaging",
        "com.google.android.gm"
    )

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        _instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.isOngoing) return
        if (sbn.packageName == packageName) return
        // SEULEMENT tes 3 apps
        if (sbn.packageName !in allowedPkgs) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (_: Exception) { sbn.packageName }

        val floating = NotificationRepository.FloatingNotif(
            sbn.id, sbn.packageName, appName, title, text, sbn.postTime, sbn.notification, sbn.key
        )

        NotificationRepository.notifs.removeAll { it.sbnKey == sbn.key }
        NotificationRepository.notifs.add(0, floating)
        if (NotificationRepository.notifs.size > 10) {
            NotificationRepository.notifs.removeAt(NotificationRepository.notifs.size - 1)
        }

        NotificationRepository.listener?.invoke()
        FloatingNotificationService.show(this)
        FloatingNotificationService.forceRefresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        NotificationRepository.notifs.removeAll { it.sbnKey == sbn.key }
        NotificationRepository.listener?.invoke()
        FloatingNotificationService.forceRefresh()
    }
}

