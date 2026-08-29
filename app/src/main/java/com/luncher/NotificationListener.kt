package com.luncher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    
    companion object {
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val GMAIL_PACKAGE = "com.google.android.gm"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val pkg = sbn.packageName
        if (pkg == "com.android.mms" || pkg == "com.google.android.apps.messaging") return
        
        val type = when (pkg) {
            WHATSAPP_PACKAGE -> "WHATSAPP"
            GMAIL_PACKAGE -> "GMAIL"
            else -> return
        }
        
        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() 
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: "Sans expéditeur"
            
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() 
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: "(Message vide)"
        
        val message = Message(
            id = sbn.key,
            type = type,
            sender = title,
            content = text,
            time = sbn.postTime,
            packageName = pkg
        )
        
        MessagePopupActivity.show(this, message)
        cancelNotification(sbn.key)
    }
}
