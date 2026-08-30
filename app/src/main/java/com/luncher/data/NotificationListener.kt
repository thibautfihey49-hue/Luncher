package com.luncher.data
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
class NotificationListener : NotificationListenerService() {
    companion object{ private var inst: NotificationListener?=null; fun getInstance()=inst }
    private val ALLOWED = setOf("com.whatsapp","com.whatsapp.w4b","com.google.android.apps.messaging","com.samsung.android.messaging","com.android.mms","com.google.android.gm")
    override fun onCreate(){ super.onCreate(); inst=this; FloatingNotificationService.show(this) }
    override fun onDestroy(){ inst=null; super.onDestroy() }
    override fun onNotificationPosted(sbn: StatusBarNotification){
        try{
            val pkg = sbn.packageName
            if(pkg==packageName || pkg=="android") return
            if(pkg !in ALLOWED) return
            val extras = sbn.notification.extras
            val txt = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            if(txt.isBlank()) return
            if(txt.contains("synchronisation", true)) return
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
