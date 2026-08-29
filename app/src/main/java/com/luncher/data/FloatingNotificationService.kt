
package com.luncher.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.luncher.R
import android.app.Service
import android.os.IBinder
import com.google.android.material.button.MaterialButton

class FloatingNotificationService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var container: LinearLayout? = null

    companion object {
        private const val CHANNEL_ID = "luncher_aggressive"
        private const val NOTIF_ID = 1001
        var instance: FloatingNotificationService? = null
        fun show(context: Context) {
            val intent = Intent(context, FloatingNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
        fun hide(context: Context) { context.stopService(Intent(context, FloatingNotificationService::class.java)) }
        fun forceRefresh() { instance?.refreshAggressive() }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createChannel()
        startForeground(NOTIF_ID, buildForegroundNotif())
        showAggressiveWindow()
        NotificationRepository.listener = { refreshAggressive() }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { showAggressiveWindow(); refreshAggressive(); return START_STICKY }
    override fun onDestroy() { super.onDestroy(); removeAggressiveWindow(); NotificationRepository.listener = null; instance = null; try { show(this) } catch (_: Exception) {} }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Luncher Agressif", NotificationManager.IMPORTANCE_LOW)
            ch.setShowBadge(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }
    private fun buildForegroundNotif(): Notification {
        val pending = PendingIntent.getActivity(this, 0, packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Luncher actif").setContentText("Popup SMS Mail WhatsApp").setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pending).setOngoing(true).build()
    }
    private fun showAggressiveWindow() {
        if (floatingView != null) return
        if (windowManager == null) windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val cont = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8,8,8,8) }
        scroll.addView(cont); container = cont
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 80 }
        floatingView = scroll
        try { windowManager?.addView(floatingView, params) } catch (e: Exception) { Toast.makeText(this, "Permission overlay requise", Toast.LENGTH_LONG).show(); return }
        refreshAggressive()
    }
    fun refreshAggressive() {
        val cont = container ?: return
        val wm = windowManager
        val view = floatingView
        if (wm == null || view == null) { showAggressiveWindow(); return }
        cont.removeAllViews()
        if (NotificationRepository.notifs.isEmpty()) { view.visibility = View.GONE; return }
        view.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        for (notif in NotificationRepository.notifs.toList()) {
            val card = inflater.inflate(R.layout.item_notification_float, cont, false)
            val iconView = card.findViewById<android.widget.ImageView>(R.id.notifIcon)
            val appNameView = card.findViewById<TextView>(R.id.notifAppName)
            val timeView = card.findViewById<TextView>(R.id.notifTime)
            val titleView = card.findViewById<TextView>(R.id.notifTitle)
            val contentView = card.findViewById<TextView>(R.id.notifContent)
            val actionsContainer = card.findViewById<LinearLayout>(R.id.notifActions)
            try { iconView.setImageDrawable(packageManager.getApplicationIcon(notif.packageName)) } catch (_: Exception) {}
            appNameView.text = notif.appName
            timeView.text = android.text.format.DateFormat.format("HH:mm", notif.time).toString()
            titleView.text = if (notif.title.isBlank()) notif.appName else notif.title
            contentView.text = notif.content
            actionsContainer.removeAllViews()
            val closeBtn = MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle).apply { text = "FERMER"; setTextColor(0xFFFF8888.toInt()) }
            closeBtn.setOnClickListener { NotificationRepository.notifs.remove(notif); try { NotificationListener.getInstance()?.cancelNotification(notif.sbnKey) } catch (_: Exception) {}; refreshAggressive() }
            actionsContainer.addView(closeBtn)
            notif.notification.actions?.forEach { action ->
                val actionTitle = action.title.toString()
                val btn = MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
                    text = actionTitle.uppercase()
                    setTextColor(when { actionTitle.contains("pondre", true) -> 0xFFFFEB3B.toInt(); actionTitle.contains("chiv", true) -> 0xFFAAFFFFFF.toInt(); else -> 0xFF2196F3.toInt() })
                }
                btn.setOnClickListener {
                    try { if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if (launch != null) startActivity(launch) else action.actionIntent.send() } else { action.actionIntent.send() } } catch (e: Exception) { try { notif.notification.contentIntent?.send() } catch (_: Exception) {} }
                    if (!actionTitle.contains("pondre", true)) { NotificationRepository.notifs.remove(notif); refreshAggressive() }
                }
                actionsContainer.addView(btn)
            }
            if (notif.notification.actions.isNullOrEmpty()) {
                val openBtn = MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle).apply { text = "OUVRIR"; setTextColor(0xFF2196F3.toInt()) }
                openBtn.setOnClickListener { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if (launch != null) startActivity(launch) else notif.notification.contentIntent?.send() } catch (_: Exception) {}; NotificationRepository.notifs.remove(notif); refreshAggressive() }
                actionsContainer.addView(openBtn)
                val replyBtn = MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle).apply { text = "REPONDRE"; setTextColor(0xFFFFEB3B.toInt()) }
                replyBtn.setOnClickListener { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if (launch != null) startActivity(launch) else notif.notification.contentIntent?.send() } catch (_: Exception) {}; NotificationRepository.notifs.remove(notif); refreshAggressive() }
                actionsContainer.addView(replyBtn)
            }
            cont.addView(card)
        }
    }
    private fun removeAggressiveWindow() { try { floatingView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}; floatingView = null; container = null }
}

