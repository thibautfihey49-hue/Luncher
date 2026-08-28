package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val onDismiss: (Message) -> Unit,
    private val onReply: (Message) -> Unit,
    private val onOpen: (Message) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.Holder>() {
    
    private var list = emptyList<Message>()
    
    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.msg_icon)
        val sender: TextView = v.findViewById(R.id.msg_sender)
        val content: TextView = v.findViewById(R.id.msg_content)
        val time: TextView = v.findViewById(R.id.msg_time)
        val dismissBtn: Button = v.findViewById(R.id.msg_dismiss)
        val replyBtn: Button = v.findViewById(R.id.msg_reply)
        val openBtn: Button = v.findViewById(R.id.msg_open)
    }
    
    override fun onCreateViewHolder(p: ViewGroup, t: Int): Holder {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_message, p, false)
        return Holder(view)
    }
    
    override fun onBindViewHolder(h: Holder, i: Int) {
        val msg = list[i]
        
        // Icône selon le type
        val iconRes = when (msg.type) {
            "SMS" -> android.R.drawable.ic_dialog_email
            "WHATSAPP" -> android.R.drawable.ic_menu_call
            "GMAIL" -> android.R.drawable.ic_dialog_info
            else -> android.R.drawable.ic_dialog_email
        }
        h.icon.setImageResource(iconRes)
        
        h.sender.text = "${msg.type} • ${msg.sender}"
        h.content.text = msg.content
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        h.time.text = timeFormat.format(Date(msg.time))
        
        h.dismissBtn.setOnClickListener { onDismiss(msg) }
        h.replyBtn.setOnClickListener { onReply(msg) }
        h.openBtn.setOnClickListener { onOpen(msg) }
    }
    
    override fun getItemCount() = list.size
    
    fun setList(new: List<Message>) {
        list = new
        notifyDataSetChanged()
    }
}
