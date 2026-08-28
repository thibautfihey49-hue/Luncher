package com.luncher.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.luncher.R
import com.luncher.data.MessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingWindowService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    companion object {
        private var instance: FloatingWindowService? = null
        
        fun showMessage(context: Context, message: MessageItem) {
            instance?.addMessage(message)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        instance = this
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        return START_STICKY
    }

    private fun addMessage(msg: MessageItem) {
        showFloatingWindow(msg)
    }

    private fun showFloatingWindow(msg: MessageItem) {
        if (floatingView != null) return

        val layout = LayoutInflater.from(this)
            .inflate(R.layout.window_floating_notification, null)
        
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 40

        layout.findViewById<TextView>(R.id.notif_app).text = msg.appName
        layout.findViewById<TextView>(R.id.notif_sender).text = msg.sender
        layout.findViewById<TextView>(R.id.notif_content).text = msg.content
        layout.findViewById<TextView>(R.id.notif_time).text = msg.time
        layout.findViewById<ImageView>(R.id.notif_icon).setImageResource(msg.icon)

        layout.findViewById<View>(R.id.btn_close).setOnClickListener {
            closeWindow()
        }

        layout.findViewById<View>(R.id.btn_open).setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(msg.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                closeWindow()
            }
        }

        layout.findViewById<Button>(R.id.btn_reply).setOnClickListener {
            val replyText = layout.findViewById<EditText>(R.id.et_reply).text.toString()
            if (replyText.isNotBlank()) {
                Toast.makeText(this, "✉️ Réponse envoyée", Toast.LENGTH_SHORT).show()
                layout.findViewById<EditText>(R.id.et_reply).text.clear()
            }
        }

        floatingView = layout
        windowManager.addView(layout, params)

        CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            closeWindow()
        }
    }

    private fun closeWindow() {
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        closeWindow()
        instance = null
    }
}
