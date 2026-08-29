package com.luncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val context: Context,
    private val notifications: MutableList<Message>,
    private val onClose: (String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.notif_icon)
        val title: TextView = view.findViewById(R.id.notif_title)
        val sender: TextView = view.findViewById(R.id.notif_sender)
        val content: TextView = view.findViewById(R.id.notif_content)
        val btnClose: Button = view.findViewById(R.id.btn_close)
        val etReply: EditText = view.findViewById(R.id.et_reply)
        val btnSendReply: Button = view.findViewById(R.id.btn_send_reply)
        val btnOpenApp: Button = view.findViewById(R.id.btn_open_app)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val msg = notifications[position]
        
        val iconRes = when (msg.type) {
            "SMS" -> android.R.drawable.ic_dialog_email
            "WHATSAPP" -> android.R.drawable.ic_menu_call
            "GMAIL" -> android.R.drawable.ic_dialog_info
            else -> android.R.drawable.ic_dialog_email
        }
        
        val titleText = when(msg.type) {
            "SMS" -> "📩 SMS"
            "WHATSAPP" -> "💬 WhatsApp"
            "GMAIL" -> "📧 Gmail"
            else -> "📬 Message"
        }
        
        val bgDrawable = when(msg.type) {
            "SMS" -> context.resources.getDrawable(R.drawable.bg_notification_sms)
            "WHATSAPP" -> context.resources.getDrawable(R.drawable.bg_notification_whatsapp)
            "GMAIL" -> context.resources.getDrawable(R.drawable.bg_notification_gmail)
            else -> context.resources.getDrawable(R.drawable.bg_notification_sms)
        }
        
        holder.icon.setImageResource(iconRes)
        holder.title.text = titleText
        holder.sender.text = msg.sender
        holder.content.text = msg.content
        holder.content.maxLines = 6
        holder.itemView.background = bgDrawable
        
        // ✅ FERMER LA NOTIFICATION
        holder.btnClose.setOnClickListener {
            onClose(msg.id)
        }
        
        // ✅ OUVRIR L'APPLICATION CONCERNÉE
        holder.btnOpenApp.setOnClickListener {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(msg.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Impossible d'ouvrir l'application", Toast.LENGTH_SHORT).show()
            }
        }
        
        // ✅ RÉPONDRE DIRECTEMENT
        holder.btnSendReply.setOnClickListener {
            val replyText = holder.etReply.text.toString().trim()
            if (replyText.isNotEmpty()) {
                when (msg.type) {
                    "SMS" -> sendSmsReply(msg.sender, replyText)
                    "WHATSAPP" -> openWhatsAppReply(msg.sender, replyText)
                    "GMAIL" -> openGmailReply(msg.sender)
                }
                holder.etReply.text.clear()
                onClose(msg.id)
            }
        }
    }

    override fun getItemCount(): Int = notifications.size

    private fun sendSmsReply(number: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            Toast.makeText(context, "✅ SMS envoyé !", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsAppReply(number: String, message: String) {
        try {
            val cleanNumber = number.replace(Regex("[^+0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$cleanNumber&text=${Uri.encode(message)}"))
            intent.setPackage("com.whatsapp")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "❌ WhatsApp non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGmailReply(sender: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$sender"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gmail non disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
