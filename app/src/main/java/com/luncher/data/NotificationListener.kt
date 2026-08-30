package com.luncher.data
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    companion object{ private var inst: NotificationListener?=null; fun getInstance()=inst }

    private val ALLOWED = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.google.android.gm"
    )

    override fun onCreate(){ super.onCreate(); inst=this; FloatingNotificationService.show(this) }
    override fun onDestroy(){ inst=null; super.onDestroy() }

    override fun onNotificationPosted(sbn: StatusBarNotification){
        try{
            val pkg = sbn.packageName
            if(pkg==packageName || pkg=="android") return
            if(pkg !in ALLOWED) return

            val notif = sbn.notification
            val extras = notif.extras
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val content = if(bigText.isNotBlank()) bigText else text

            // FORCE UNIQUEMENT MESSAGES REÇUS AVEC DU VRAI TEXTE
            if(content.isBlank()) return
            if(content.length < 2) return
            // Bloque les notifs système Gmail "Synchronisation"
            if(content.contains("synchronisation", true)) return
            if(content.contains("Synchronisation", true)) return

            val pm=packageManager
            val appName=try{ pm.getApplicationLabel(pm.getApplicationInfo(pkg,0)).toString() }catch(_:Exception){ pkg }
            NotificationRepository.add(sbn, appName)
            FloatingNotificationService.show(this)
            FloatingNotificationService.forceRefresh()
        }catch(_:Exception){}
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification){ try{ NotificationRepository.remove(sbn.key); FloatingNotificationService.forceRefresh() }catch(_:Exception){} }
    fun cancelNotif(k: String){ try{ cancelNotification(k) }catch(_:Exception){} }
}
