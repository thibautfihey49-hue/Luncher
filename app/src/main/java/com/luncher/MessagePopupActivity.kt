package com.luncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class MessagePopupActivity : Activity() {

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_SENDER = "sender"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_PACKAGE = "package"
        
        fun show(context: Context, message: Message) {
            val intent = Intent(context, MessagePopupActivity::class.java).apply {
                putExtra(EXTRA_TYPE, message.type)
                putExtra(EXTRA_SENDER, message.sender)
                putExtra(EXTRA_CONTENT, message.content)
                putExtra(EXTRA_PACKAGE, message.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_message)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: "SMS"
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: ""
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val packageName = intent.getStringExtra(EXTRA_PACKAGE) ?: ""

        val icon = findViewById<ImageView>(R.id.popup_icon)
        val title = findViewById<TextView>(R.id.popup_title)
        val senderTv = findViewById<TextView>(R.id.popup_sender)
        val contentTv = findViewById<TextView>(R.id.popup_content)
        val etReply = findViewById<EditText>(R.id.popup_reply)
        val btnSend = findViewById<Button>(R.id.btn_send)
        val btnClose = findViewById<Button>(R.id.btn_close)
        val btnOpen = findViewById<Button>(R.id.btn_open_app)

        when (type) {
            "SMS" -> {
                icon.setImageResource(android.R.drawable.ic_dialog_email)
                title.text = "📩 SMS"
            }
            "WHATSAPP" -> {
                icon.setImageResource(android.R.drawable.ic_menu_call)
                title.text = "💬 WhatsApp"
            }
            "GMAIL" -> {
                icon.setImageResource(android.R.drawable.ic_dialog_info)
                title.text = "📧 Gmail"
            }
        }

        senderTv.text = sender
        contentTv.text = content

        btnClose.setOnClickListener { finish() }

        btnOpen.setOnClickListener {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Impossible d'ouvrir l'application", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            val replyText = etReply.text.toString().trim()
            if (replyText.isNotEmpty()) {
                when (type) {
                    "SMS" -> sendSms(sender, replyText)
                    "WHATSAPP" -> openWhatsApp(sender, replyText)
                    "GMAIL" -> openGmail(sender)
                }
                finish()
            }
        }
    }

    private fun sendSms(number: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            Toast.makeText(this, "✅ SMS envoyé !", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(number: String, message: String) {
        try {
            val cleanNumber = number.replace(Regex("[^+0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$cleanNumber&text=${Uri.encode(message)}"))
            intent.setPackage("com.whatsapp")
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ WhatsApp non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGmail(sender: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$sender"))
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Gmail non disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
