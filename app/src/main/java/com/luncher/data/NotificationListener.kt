package com.luncher.data
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    companion object{ private var inst: NotificationListener?=null; fun getInstance()=inst }

    // SEULEMENT MESSAGES
    private val ALLOWED = setOf(
        "com.whatsapp", // WhatsApp
        "com.whatsapp.w4b", // WhatsApp Business
        "com.google.android.apps.messaging", // Google Messages (SMS)
        "com.samsung.android.messaging", // Samsung SMS
        "com.android.mms", // SMS générique
        "com.google.android.gm", // Gmail
        "com.microsoft.android.outlook", // Outlook si tu l'as
        "com.facebook.orca", // Messenger (optionnel, enlève si tu veux pas)
        "org.telegram.messenger", // Telegram (optionnel)
        "com.instagram.android" // Insta DM (optionnel)
    )

    override fun onCreate(){ super.onCreate(); inst=this; FloatingNotificationService.show(this) }
    override fun onDestroy(){ inst=null; super.onDestroy() }

    override fun onNotificationPosted(sbn: StatusBarNotification){
        try{
            val pkg = sbn.packageName
            if(pkg==packageName || pkg=="android") return

            // FILTRE PRINCIPAL
            if(pkg !in ALLOWED) return

            // Filtre anti-spam : seulement les notifs qui ont du texte et catégorie message
            val notif = sbn.notification
            val isMessage = notif.category == Notification.CATEGORY_MESSAGE || notif.category == Notification.CATEGORY_EMAIL || notif.category == Notification.CATEGORY_SOCIAL
            val hasRemoteInput = notif.actions?.any{ it.remoteInputs!=null && it.remoteInputs.isNotEmpty() } == true
            
            // Pour SMS/MMS on accepte même sans category
            val isSmsPkg = pkg.contains("messaging") || pkg.contains("mms")
            
            // Si c'est Gmail/WhatsApp mais pas un message (ex: "Synchronisation..."), on ignore
            if(!isMessage && !hasRemoteInput && !isSmsPkg){
                // Gmail sync, "1 nouveau message" sans texte, etc -> ignore
                val e = notif.extras
                val txt = e.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                if(txt.isBlank() || txt.contains("synchronisation", true) || txt.contains("en cours", true)) return
            }

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
