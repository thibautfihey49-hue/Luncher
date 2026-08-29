
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
        var instance: NotificationListener? = null
        fun getInstance(): NotificationListener? = instance
    }

    // SMS, Mail, WhatsApp agressif
    private val allowedPkgs = setOf(
        "com.google.android.gm", // Gmail
        "com.microsoft.office.outlook",
        "com.yahoo.mobile.client.android.mail",
        "com.google.android.apps.messaging", // Messages
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.whatsapp", // WhatsApp
        "com.whatsapp.w4b",
        "com.facebook.orca", // Messenger
        "org.telegram.messenger",
        "com.viber.voip",
        "com.signal"
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.isOngoing) return
        if (sbn.packageName == packageName) return // ignore nos propres notifs

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        // AGRESSIF: on prend tout ce qui est message OU qui est dans la liste
        val isMessageCat = sbn.notification.category == Notification.CATEGORY_MESSAGE || sbn.notification.category == Notification.CATEGORY_EMAIL || sbn.notification.category == Notification.CATEGORY_SOCIAL
        val isAllowedPkg = sbn.packageName in allowedPkgs
        val isMessagingStyle = extras.getCharSequence(Notification.EXTRA_TEXT) != null

        if (!isAllowedPkg && !isMessageCat) {
            // Pour SMS/Mail/WhatsApp on est agressif: on accepte meme sans category si package connu par nom
            if (!sbn.packageName.contains("mms") && !sbn.packageName.contains("messag") && !sbn.packageName.contains("mail") && !sbn.packageName.contains("whatsapp") && !sbn.packageName.contains("gmail") && !sbn.packageName.contains("outlook") && !sbn.packageName.contains("telegram")) {
                // On filtre moins agressif maintenant, on prend tout ce qui a du texte pour etre permanent
                if (sbn.packageName !in allowedPkgs && sbn.notification.category != null && sbn.notification.category != Notification.CATEGORY_MESSAGE) {
                    // Laisse passer quand meme si c'est un message avec du texte
                    if (text.length < 3) return
                }
            }
        }

        val appName = try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (e: Exception) { sbn.packageName }

        val floating = NotificationRepository.FloatingNotif(
            sbn.id, sbn.packageName, appName, title, text, sbn.postTime, sbn.notification, sbn.key
        )

        // Remplace si deja existe
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
        if (NotificationRepository.notifs.isEmpty()) {
            // Ne cache pas immediatement pour rester permanent, mais cache si vide
            FloatingNotificationService.instance?.refreshAggressive()
        }
    }
}

