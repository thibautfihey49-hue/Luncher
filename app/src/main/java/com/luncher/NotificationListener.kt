package com.luncher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationListener : NotificationListenerService() {
    companion object {
        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val GMAIL_PACKAGE = "com.google.android.gm"
        const val SMS_PACKAGE = "com.android.mms"
        const val SMS_PACKAGE2 = "com.google.android.apps.messaging"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val pkg = sbn.packageName
        val type = when (pkg) {
            WHATSAPP_PACKAGE -> "WHATSAPP"
            GMAIL_PACKAGE -> "GMAIL"
            SMS_PACKAGE, SMS_PACKAGE2 -> "SMS"
            else -> return
        }
        
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Sans expéditeur"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "(Pas de contenu)"
        
        val message = Message(
            id = sbn.key,
            type = type,
            sender = title,
            content = text,
            time = sbn.postTime,
            packageName = pkg
        )
        addMessage(message)
    }
    
    private fun addMessage(message: Message) {
        messagesFlow.value = listOf(message) + messagesFlow.value
    }
    
    fun removeMessage(id: String) {
        messagesFlow.value = messagesFlow.value.filter { it.id != id }
    }
}
