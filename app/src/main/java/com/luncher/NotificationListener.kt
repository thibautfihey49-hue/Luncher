package com.luncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationListener : NotificationListenerService() {
    
    companion object {
        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val GMAIL_PACKAGE = "com.google.android.gm"
        const val SMS_PACKAGE = "com.android.mms"
        const val SMS_PACKAGE2 = "com.google.android.apps.messaging"
        private var instance: NotificationListener? = null
        
        fun getInstance(): NotificationListener? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startForegroundService()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val CHANNEL_ID = "Luncher_Notifications_Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Service Notifications Luncher",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Garde les notifications visibles par-dessus toutes les applications"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Luncher actif")
            .setContentText("Les notifications s'affichent par-dessus toutes les applications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        
        startForeground(1001, notification)
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
        
        // ✅ Récupère TOUTES les infos, y compris le texte complet
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() 
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: "Sans expéditeur"
            
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() 
            ?: extras.getCharSequence(Notification.EXTRA_TEXT_LINES)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: "(Message vide)"
        
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() ?: ""
        
        val fullContent = when {
            text.isNotEmpty() && subText.isNotEmpty() -> "$text\n$subText"
            text.isNotEmpty() && infoText.isNotEmpty() -> "$text\n$infoText"
            else -> text
        }
        
        val message = Message(
            id = sbn.key,
            type = type,
            sender = title,
            content = fullContent,
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

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
