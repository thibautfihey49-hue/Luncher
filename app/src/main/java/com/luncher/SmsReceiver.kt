package com.luncher
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
class SmsReceiver: BroadcastReceiver(){
    override fun onReceive(c: Context, intent: Intent){
        try{
            if(intent.action==Telephony.Sms.Intents.SMS_RECEIVED_ACTION){
                val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if(msgs.isNotEmpty()){
                    val from = msgs[0].originatingAddress?: "Inconnu"
                    val body = msgs.joinToString(""){ it.messageBody }
                    OverlayNotifService.showSms(c, from, body)
                }
            }
        }catch(_:Exception){}
    }
}
