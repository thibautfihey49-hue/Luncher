package com.luncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            
            val messages: Array<SmsMessage> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
            } else {
                @Suppress("DEPRECATION")
                val pdus = intent.extras?.get("pdus") as? Array<*>
                pdus?.map { SmsMessage.createFromPdu(it as ByteArray) }?.toTypedArray() ?: emptyArray()
            }
            
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val body = sms.messageBody
                val time = sms.timestampMillis
                
                val message = Message(
                    id = "sms_${time}_${sender.hashCode()}",
                    type = "SMS",
                    sender = sender,
                    content = body,
                    time = time,
                    packageName = "com.android.mms"
                )
                
                FloatingWindowService.showMessage(context, message)
                // ✅ PAS de abortBroadcast() → le SMS va aussi dans la boîte de réception
            }
        }
    }
}
