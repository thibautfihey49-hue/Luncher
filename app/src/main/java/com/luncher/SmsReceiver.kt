package com.luncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for ((index, sms) in messages.withIndex()) {
                val message = Message(
                    id = "sms_${System.currentTimeMillis()}_$index",
                    type = "SMS",
                    sender = sms.displayOriginatingAddress ?: "Inconnu",
                    content = sms.displayMessageBody ?: "",
                    time = System.currentTimeMillis(),
                    packageName = "sms"
                )
                NotificationListener.messagesFlow.value = 
                    listOf(message) + NotificationListener.messagesFlow.value
            }
        }
    }
}
