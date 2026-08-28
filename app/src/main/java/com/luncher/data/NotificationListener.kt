package com.luncher.data

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.luncher.ui.FloatingWindowService
import java.text.SimpleDateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {
    private val watchedApps = listOf(
        "com.whatsapp",
        "com.google.android.gm",
        "com.android.mms"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val pkg = sbn.packageName
        if (!watchedApps.contains(pkg)) return
        
        val notification = sbn.notification
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Message"
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        val appName = when (pkg) {
            "com.whatsapp" -> "WhatsApp"
            "com.google.android.gm" -> "Gmail"
            "com.android.mms" -> "SMS"
            else -> pkg
        }
        
        val iconRes = when (pkg) {
            "com.whatsapp" -> android.R.drawable.ic_menu_call
            "com.google.android.gm" -> android.R.drawable.ic_dialog_email
            else -> android.R.drawable.ic_dialog_info
        }
        
        val timeStr = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())
        
        val message = MessageItem(
            id = System.currentTimeMillis(),
            appName = appName,
            packageName = pkg,
            sender = title,
            content = text,
            time = timeStr,
            icon = iconRes
        )
        FloatingWindowService.showMessage(this, message)
    }
}
