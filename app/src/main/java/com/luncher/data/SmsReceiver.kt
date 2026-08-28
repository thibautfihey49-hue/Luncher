package com.luncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.luncher.ui.FloatingWindowService
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val msg = msgs.firstOrNull() ?: return
            
            val timeStr = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())
            val message = MessageItem(
                id = System.currentTimeMillis(),
                appName = "SMS",
                packageName = "com.android.mms",
                sender = msg.displayOriginatingAddress ?: "Inconnu",
                content = msg.messageBody ?: "",
                time = timeStr,
                icon = android.R.drawable.ic_dialog_email
            )
            FloatingWindowService.showMessage(context, message)
        }
    }
}
