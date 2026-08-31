package com.luncher.notifications

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

data class LuncherNotif(
    val key:String,
    val pkg:String,
    val title:String,
    val text:String,
    val time:Long,
    val actions:List<Notification.Action>,
    val compatActions:List<NotificationCompat.Action>,
    val sbn: StatusBarNotification
)

object NotificationStore{
    var notifs = mutableListOf<LuncherNotif>()
}

class LuncherNotificationService: NotificationListenerService(){
    init{ instance=this }
    override fun onNotificationPosted(sbn: StatusBarNotification?){
        sbn?:return
        val pkg=sbn.packageName
        if(pkg!in listOf("com.whatsapp","com.whatsapp.w4b","com.google.android.gm","com.android.mms","com.google.android.apps.messaging","com.facebook.orca","org.telegram.messenger")) return
        val extras=sbn.notification.extras
        val title=extras.getString("android.title")?: pkg
        val text=extras.getCharSequence("android.text")?.toString()?: extras.getCharSequence("android.bigText")?.toString()?: ""
        if(text.isBlank()) return
        val actions=sbn.notification.actions?.toList()?: emptyList()
        val compatActions=NotificationCompat.WearableExtender(sbn.notification).actions
        val notif=LuncherNotif(sbn.key, pkg, title, text, sbn.postTime, actions, compatActions, sbn)
        NotificationStore.notifs.removeAll{ it.key==sbn.key }
        NotificationStore.notifs.add(0,notif)
        if(NotificationStore.notifs.size>20) NotificationStore.notifs = NotificationStore.notifs.take(20).toMutableList()
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?){
        sbn?.let{ NotificationStore.notifs.removeAll{ n-> n.key==it.key } }
    }
    companion object{
        var instance:LuncherNotificationService? = null
        fun sendQuickReply(notif: LuncherNotif, replyText:String){
            try{
                val ctx = instance?.applicationContext
                for(action in notif.actions){
                    val rInputs = action.remoteInputs
                    if(!rInputs.isNullOrEmpty()){
                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putString(rInputs[0].resultKey, replyText)
                        val results = rInputs.map{ android.app.RemoteInput.Builder(it.resultKey).build() }.toTypedArray()
                        android.app.RemoteInput.addResultsToIntent(results, intent, bundle)
                        if(ctx!=null) action.actionIntent.send(ctx,0,intent) else action.actionIntent.send()
                        return
                    }
                }
                for(action in notif.compatActions){
                    val rInputs = action.remoteInputs
                    if(!rInputs.isNullOrEmpty()){
                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putString(rInputs[0].resultKey, replyText)
                        RemoteInput.addResultsToIntent(rInputs, intent, bundle)
                        if(ctx!=null) action.actionIntent?.send(ctx,0,intent) else action.actionIntent?.send()
                        return
                    }
                }
            }catch(e:Exception){ e.printStackTrace() }
        }
    }
}
