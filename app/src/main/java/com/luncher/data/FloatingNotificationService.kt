package com.luncher.data
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.luncher.R
class FloatingNotificationService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    companion object {
        private const val CHANNEL_ID = "floating_channel"
        private const val NOTIF_ID = 1001
        fun show(context: Context) { val intent = Intent(context, FloatingNotificationService::class.java); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent) }
        fun hide(context: Context) { context.stopService(Intent(context, FloatingNotificationService::class.java)) }
    }
    override fun onCreate() { super.onCreate(); createChannel(); startForeground(NOTIF_ID, buildForegroundNotif()); showFloatingWindow(); NotificationRepository.listener = { refreshFloatingWindow() } }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { refreshFloatingWindow(); return START_STICKY }
    override fun onDestroy() { super.onDestroy(); removeFloatingWindow(); NotificationRepository.listener = null }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { val ch = NotificationChannel(CHANNEL_ID, "Luncher Floating", NotificationManager.IMPORTANCE_LOW); val nm = getSystemService(NotificationManager::class.java); nm.createNotificationChannel(ch) } }
    private fun buildForegroundNotif(): Notification { val pending = PendingIntent.getActivity(this, 0, packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT); return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Luncher").setContentText("Affichage des messages").setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pending).setOngoing(true).build() }
    private fun showFloatingWindow() {
        if (floatingView != null) return
        if (NotificationRepository.notifs.isEmpty()) return
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val scroll = ScrollView(this); val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; scroll.addView(container)
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 100 }
        floatingView = scroll
        try { windowManager?.addView(floatingView, params) } catch (e: Exception) { return }
        refreshFloatingWindow()
    }
    private fun refreshFloatingWindow() {
        val view = floatingView as? ScrollView ?: return
        val container = view.getChildAt(0) as? LinearLayout ?: return
        container.removeAllViews()
        if (NotificationRepository.notifs.isEmpty()) { removeFloatingWindow(); stopSelf(); return }
        val inflater = LayoutInflater.from(this)
        for (notif in NotificationRepository.notifs) {
            val card = inflater.inflate(R.layout.item_notification_float, container, false)
            val iconView = card.findViewById<android.widget.ImageView>(R.id.notifIcon)
            val appNameView = card.findViewById<android.widget.TextView>(R.id.notifAppName)
            val timeView = card.findViewById<android.widget.TextView>(R.id.notifTime)
            val titleView = card.findViewById<android.widget.TextView>(R.id.notifTitle)
            val contentView = card.findViewById<android.widget.TextView>(R.id.notifContent)
            val btnClose = card.findViewById<View>(R.id.btnClose)
            val btnOpen = card.findViewById<View>(R.id.btnOpen)
            val btnReply = card.findViewById<View>(R.id.btnReply)
            try { iconView.setImageDrawable(packageManager.getApplicationIcon(notif.packageName)) } catch (e: Exception) {}
            appNameView.text = notif.appName; timeView.text = android.text.format.DateFormat.format("HH:mm", notif.time); titleView.text = notif.title; contentView.text = notif.content
            btnClose.setOnClickListener { NotificationRepository.notifs.remove(notif); refreshFloatingWindow() }
            btnOpen.setOnClickListener { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch) } else { notif.notification.contentIntent?.send() } } catch (e: Exception) { Toast.makeText(this, "Impossible", Toast.LENGTH_SHORT).show() }; NotificationRepository.notifs.remove(notif); refreshFloatingWindow() }
            btnReply.setOnClickListener { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch) } catch (e: Exception) {}; NotificationRepository.notifs.remove(notif); refreshFloatingWindow() }
            container.addView(card)
        }
    }
    private fun removeFloatingWindow() { try { floatingView?.let { windowManager?.removeView(it) } } catch (e: Exception) {}; floatingView = null }
}
