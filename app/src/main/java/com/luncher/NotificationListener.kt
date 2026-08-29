package com.luncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "LUNCHER_MESSAGES",
                "Messages Luncher",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications des messages SMS, WhatsApp et Gmail"
                enableVibration(true)
                setShowBadge(true)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
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
        
        // ✅ 1. Ajoute à la liste
        messagesFlow.value = listOf(message) + messagesFlow.value
        
        // ✅ 2. Affiche une NOTIFICATION SYSTÈME (fiable à 100%)
        showSystemNotification(message)
        
        // ✅ 3. OUVRE LA POPUP DIRECTEMENT DEVANT TOUT !
        try {
            MessagePopupActivity.show(this, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showSystemNotification(message: Message) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // ✅ Intent pour OUVRIR LA POPUP quand on clique sur la notification
        val intent = Intent(this, MessagePopupActivity::class.java).apply {
            putExtra(MessagePopupActivity.EXTRA_TYPE, message.type)
            putExtra(MessagePopupActivity.EXTRA_SENDER, message.sender)
            putExtra(MessagePopupActivity.EXTRA_CONTENT, message.content)
            putExtra(MessagePopupActivity.EXTRA_PACKAGE, message.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            message.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val iconRes = when (message.type) {
            "SMS" -> android.R.drawable.ic_dialog_email
            "WHATSAPP" -> android.R.drawable.ic_menu_call
            "GMAIL" -> android.R.drawable.ic_dialog_info
            else -> android.R.drawable.ic_dialog_email
        }
        
        val titleText = when(message.type) {
            "SMS" -> "📩 SMS"
            "WHATSAPP" -> "💬 WhatsApp"
            "GMAIL" -> "📧 Gmail"
            else -> "📬 Message"
        }
        
        val notification = NotificationCompat.Builder(this, "LUNCHER_MESSAGES")
            .setSmallIcon(iconRes)
            .setContentTitle("$titleText : ${message.sender}")
            .setContentText(message.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .build()
        
        nm.notify(message.id.hashCode(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
