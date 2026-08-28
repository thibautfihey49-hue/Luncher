package com.luncher

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationListener : NotificationListenerService() {
    companion object {
        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val GMAIL_PACKAGE = "com.google.android.gm"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val pkg = sbn.packageName
        val notification = sbn.notification
        
        val type = when (pkg) {
            WHATSAPP_PACKAGE -> "WHATSAPP"
            GMAIL_PACKAGE -> "GMAIL"
            else -> return
        }
        
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
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Optionnel : supprimer le message quand la notification est fermée
    }
    
    private fun addMessage(message: Message) {
        val current = messagesFlow.value.toMutableList()
        current.add(0, message) // Nouveaux messages EN HAUT
        messagesFlow.value = current
    }
}
