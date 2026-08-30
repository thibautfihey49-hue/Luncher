package com.luncher.data
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
class NotificationListener : NotificationListenerService() {
    companion object{ private var inst: NotificationListener?=null; fun getInstance()=inst }
    override fun onCreate(){ super.onCreate(); inst=this; FloatingNotificationService.show(this) }
    override fun onDestroy(){ inst=null; super.onDestroy() }
    override fun onNotificationPosted(sbn: StatusBarNotification){
        try{
            if(sbn.packageName==packageName) return
            val pm=packageManager
            val appName=try{pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName,0)).toString()}catch(_:Exception){sbn.packageName}
            NotificationRepository.add(sbn, appName)
            FloatingNotificationService.show(this)
            FloatingNotificationService.forceRefresh()
        }catch(_:Exception){}
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification){ try{NotificationRepository.remove(sbn.key); FloatingNotificationService.forceRefresh()}catch(_:Exception){} }
    fun cancelNotif(k: String){ try{cancelNotification(k)}catch(_:Exception){} }
}
