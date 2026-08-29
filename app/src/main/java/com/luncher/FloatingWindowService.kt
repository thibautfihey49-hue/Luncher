package com.luncher

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FloatingWindowService : Service() {

    companion object {
        private var windowManager: WindowManager? = null
        private var floatingView: View? = null
        private var messages = mutableListOf<Message>()
        private var adapter: MessageWindowAdapter? = null
        private var isShowing = false

        fun showMessage(context: Context, message: Message) {
            messages.add(0, message)
            if (isShowing) {
                adapter?.notifyItemInserted(0)
                adapter?.notifyDataSetChanged()
            } else {
                createWindow(context)
            }
        }

        private fun createWindow(context: Context) {
            isShowing = true
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            
            floatingView = inflater.inflate(R.layout.floating_notification_window, null)
            
            val layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
            }
            
            layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.92).toInt()
            
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager?.addView(floatingView, layoutParams)
            
            val recycler = floatingView!!.findViewById<RecyclerView>(R.id.messages_list)
            recycler.layoutManager = LinearLayoutManager(context)
            adapter = MessageWindowAdapter(context, messages) { position ->
                messages.removeAt(position)
                adapter?.notifyItemRemoved(position)
                if (messages.isEmpty()) {
                    closeWindow()
                }
            }
            recycler.adapter = adapter
            
            val closeAll = floatingView!!.findViewById<TextView>(R.id.close_all)
            closeAll.setOnClickListener {
                messages.clear()
                closeWindow()
            }
        }

        private fun closeWindow() {
            isShowing = false
            floatingView?.let { windowManager?.removeView(it) }
            floatingView = null
            adapter = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { super.onDestroy(); closeWindow() }
}
