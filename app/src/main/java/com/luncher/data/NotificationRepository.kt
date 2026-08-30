package com.luncher.data
import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
data class NotifItem(val sbnKey: String, val packageName: String, val appName: String, val title: String, val content: String, val time: Long, val notification: Notification)
object NotificationRepository {
    val notifs = mutableListOf<NotifItem>()
    var listener: (() -> Unit)? = null
    fun add(sbn: StatusBarNotification, appName: String){
        try{
            val n=sbn.notification
            val e=n.extras
            val title=e.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: e.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString() ?: appName
            val content=e.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: e.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: e.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""
            notifs.removeAll{it.sbnKey==sbn.key}
            notifs.add(0, NotifItem(sbn.key, sbn.packageName, appName, title, content, System.currentTimeMillis(), n))
            if(notifs.size>20) notifs.removeAt(notifs.size-1)
            listener?.invoke()
        }catch(_:Exception){}
    }
    fun remove(key: String){ notifs.removeAll{it.sbnKey==key}; listener?.invoke() }
}
