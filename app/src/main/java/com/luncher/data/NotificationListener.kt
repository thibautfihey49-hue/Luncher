package com.luncher.data; import android.app.Notification; import android.service.notification.*; import com.luncher.ui.FloatingWindowService; import java.text.SimpleDateFormat; import java.util.*
class NotificationListener : NotificationListenerService() {
    private val watched=listOf("com.whatsapp","com.google.android.gm","com.android.mms")
    override fun onNotificationPosted(sbn: StatusBarNotification){
        super.onNotificationPosted(sbn)
        val pkg=sbn.packageName; if(!watched.contains(pkg))return
        val n=sbn.notification; val title=n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?:"Message"
        val text=n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?:""
        val appName=when(pkg){"com.whatsapp"->"WhatsApp";"com.google.android.gm"->"Gmail";"com.android.mms"->"SMS";else->pkg}
        val icon=when(pkg){"com.whatsapp"->android.R.drawable.ic_menu_call;"com.google.android.gm"->android.R.drawable.ic_dialog_email;else->android.R.drawable.ic_dialog_info}
        val m=MessageItem(appName,pkg,title,text,SimpleDateFormat("HH:mm",Locale.FRANCE).format(Date()),icon)
        FloatingWindowService.showMessage(this,m)
    }
}
