
package com.luncher.data
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
object NotificationRepository {
    data class FloatingNotif(val id: Int, val packageName: String, val appName: String, val title: String, val content: String, val time: Long, val notification: Notification, val sbnKey: String)
    val notifs = mutableListOf<FloatingNotif>()
    var listener: (() -> Unit)? = null
}
class NotificationListener : NotificationListenerService() {
    private val allowedPkgs = setOf("com.google.android.gm","com.microsoft.office.outlook","com.outlook","com.yahoo.mobile.client.android.mail","com.google.android.apps.messaging","com.android.mms","com.samsung.android.messaging","com.whatsapp","com.whatsapp.w4b")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.isOngoing) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        if (title.isBlank() && text.isBlank()) return
        val isMessage = sbn.notification.category == Notification.CATEGORY_MESSAGE || sbn.packageName in allowedPkgs
        if (!isMessage && sbn.packageName !in allowedPkgs) { if (sbn.notification.category != Notification.CATEGORY_MESSAGE) return }
        val appName = try { val pm = packageManager; pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString() } catch (e: Exception) { sbn.packageName }
        val floating = NotificationRepository.FloatingNotif(sbn.id, sbn.packageName, appName, title, text, sbn.postTime, sbn.notification, sbn.key)
        NotificationRepository.notifs.removeAll { it.sbnKey == sbn.key }
        NotificationRepository.notifs.add(0, floating)
        if (NotificationRepository.notifs.size > 20) NotificationRepository.notifs.removeAt(NotificationRepository.notifs.size-1)
        NotificationRepository.listener?.invoke()
        FloatingNotificationService.show(this)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) { if (sbn == null) return; NotificationRepository.notifs.removeAll { it.sbnKey == sbn.key }; NotificationRepository.listener?.invoke(); if (NotificationRepository.notifs.isEmpty()) FloatingNotificationService.hide(this) }
}

