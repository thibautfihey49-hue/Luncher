package com.luncher.data; import android.content.*; import android.provider.Telephony; import android.telephony.SmsMessage; import com.luncher.ui.FloatingWindowService; import java.text.SimpleDateFormat; import java.util.*
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION==intent.action){
            val msg=Telephony.Sms.Intents.getMessagesFromIntent(intent).firstOrNull()?:return
            val m=MessageItem("SMS","com.android.mms",msg.displayOriginatingAddress,msg.messageBody,SimpleDateFormat("HH:mm",Locale.FRANCE).format(Date()),android.R.drawable.ic_dialog_email)
            FloatingWindowService.showMessage(context,m)
        }
    }
}
