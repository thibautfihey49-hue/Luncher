package com.luncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val message = Message(
                    id = sms.messageId.toString(),
                    type = "SMS",
                    sender = sms.displayOriginatingAddress,
                    content = sms.displayMessageBody,
                    time = System.currentTimeMillis(),
                    packageName = "sms"
                )
                NotificationListener.messagesFlow.value = 
                    listOf(message) + NotificationListener.messagesFlow.value
            }
        }
    }
}
