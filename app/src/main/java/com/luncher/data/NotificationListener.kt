package com.luncher.data
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
class NotificationListener : NotificationListenerService() {
    companion object{ private var instance: NotificationListener?=null; fun getInstance()=instance }
    override fun onCreate(){ super.onCreate(); instance=this }
    override fun onDestroy(){ instance=null; super.onDestroy() }
    override fun onNotificationPosted(sbn: StatusBarNotification){
        try{
            if(sbn.packageName==packageName) return
            val pm=packageManager
            val appName=try{pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName,0)).toString()}catch(_:Exception){sbn.packageName}
            NotificationRepository.add(sbn,appName)
        }catch(_:Exception){}
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification){ try{NotificationRepository.remove(sbn.key)}catch(_:Exception){} }
    fun cancelNotif(key: String){ try{cancelNotification(key)}catch(_:Exception){} }
}
