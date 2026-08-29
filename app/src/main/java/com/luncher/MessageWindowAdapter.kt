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
import java.text.SimpleDateFormat
import java.util.*

class MessageWindowAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<MessageWindowAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.msg_icon)
        val type: TextView = view.findViewById(R.id.msg_type)
        val sender: TextView = view.findViewById(R.id.msg_sender)
        val time: TextView = view.findViewById(R.id.msg_time)
        val content: TextView = view.findViewById(R.id.msg_content)
        val close: TextView = view.findViewById(R.id.msg_close)
        val replyBtn: Button = view.findViewById(R.id.msg_reply_btn)
        val replyArea: View = view.findViewById(R.id.reply_area)
        val replyInput: EditText = view.findViewById(R.id.msg_reply_input)
        val sendBtn: Button = view.findViewById(R.id.msg_send_reply)
        val openBtn: Button = view.findViewById(R.id.msg_open_app)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_floating_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        
        when (msg.type) {
            "SMS" -> {
                holder.icon.setImageResource(android.R.drawable.ic_dialog_email)
                holder.type.text = "📩 SMS"
            }
            "WHATSAPP" -> {
                holder.icon.setImageResource(android.R.drawable.ic_menu_call)
                holder.type.text = "💬 WhatsApp"
            }
            "GMAIL" -> {
                holder.icon.setImageResource(android.R.drawable.ic_dialog_info)
                holder.type.text = "📧 Gmail"
            }
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        holder.time.text = timeFormat.format(Date(msg.time))
        holder.sender.text = msg.sender
        holder.content.text = msg.content

        holder.close.setOnClickListener {
            onRemove(position)
        }

        holder.replyBtn.setOnClickListener {
            holder.replyArea.visibility = if (holder.replyArea.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        holder.sendBtn.setOnClickListener {
            val replyText = holder.replyInput.text.toString().trim()
            if (replyText.isNotEmpty()) {
                when (msg.type) {
                    "SMS" -> sendSms(msg.sender, replyText)
                    "WHATSAPP" -> openWhatsApp(msg.sender, replyText)
                    "GMAIL" -> openGmail(msg.sender)
                }
                holder.replyInput.text.clear()
                holder.replyArea.visibility = View.GONE
            }
        }

        // ✅ CORRECTION : FLAG_ACTIVITY_NEW_TASK OBLIGATOIRE depuis un Service
        holder.openBtn.setOnClickListener {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(msg.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "❌ Application introuvable", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSms(number: String, message: String) {
        try {
            SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
            Toast.makeText(context, "✅ SMS envoyé !", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Erreur SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(number: String, message: String) {
        try {
            val clean = number.replace(Regex("[^+0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$clean&text=${Uri.encode(message)}"))
            intent.setPackage("com.whatsapp")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "❌ WhatsApp indisponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGmail(sender: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$sender"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gmail indisponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = messages.size
}
