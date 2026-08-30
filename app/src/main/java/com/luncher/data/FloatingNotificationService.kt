package com.luncher.data
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import com.luncher.R
import android.app.Service
import android.os.IBinder
class FloatingNotificationService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var container: LinearLayout? = null
    private var themedContext: Context? = null
    private var windowParams: WindowManager.LayoutParams? = null
    companion object {
        private const val CHANNEL_ID = "luncher_aggressive"
        private const val NOTIF_ID = 1001
        var instance: FloatingNotificationService? = null
        fun show(context: Context) { val intent = Intent(context, FloatingNotificationService::class.java); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent) }
        fun hide(context: Context) { context.stopService(Intent(context, FloatingNotificationService::class.java)) }
        fun forceRefresh() { instance?.refreshAggressive() }
    }
    override fun onCreate() { super.onCreate(); instance = this; themedContext = ContextThemeWrapper(this, R.style.Theme_Luncher); createChannel(); startForeground(NOTIF_ID, buildForegroundNotif()); showAggressiveWindow(); NotificationRepository.listener = { refreshAggressive() } }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { showAggressiveWindow(); refreshAggressive(); return START_STICKY }
    override fun onDestroy() { super.onDestroy(); removeAggressiveWindow(); NotificationRepository.listener = null; instance = null; try { show(this) } catch (_: Exception) {} }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { val ch = NotificationChannel(CHANNEL_ID, "Luncher Agressif", NotificationManager.IMPORTANCE_LOW); ch.setShowBadge(false); getSystemService(NotificationManager::class.java).createNotificationChannel(ch) } }
    private fun buildForegroundNotif(): Notification { val pending = PendingIntent.getActivity(this, 0, packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT); return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Luncher actif").setContentText("Popup").setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pending).setOngoing(true).build() }
    private fun showAggressiveWindow() { if (floatingView != null) { try { windowManager?.removeView(floatingView) } catch (_: Exception) {} ; floatingView = null }; if (windowManager == null) windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager; val ctx = themedContext ?: this; val scroll = ScrollView(ctx).apply { isVerticalScrollBarEnabled = false }; val cont = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(8,8,8,8) }; scroll.addView(cont); container = cont; val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 80; softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE }; windowParams = params; floatingView = scroll; try { windowManager?.addView(floatingView, params) } catch (e: Exception) { Toast.makeText(this, "Permission overlay requise", Toast.LENGTH_LONG).show(); return }; refreshAggressive() }
    private fun enableKeyboard(edit: EditText) { try { val params = windowParams; if (params != null) { params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv(); params.flags = params.flags and WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv(); windowManager?.updateViewLayout(floatingView, params) }; edit.isFocusable = true; edit.isFocusableInTouchMode = true; edit.requestFocus(); val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager; imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT) } catch (_: Exception) {} }
    private fun disableKeyboard() { try { val params = windowParams; if (params != null) { params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; windowManager?.updateViewLayout(floatingView, params) }; val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager; floatingView?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) } } catch (_: Exception) {} }
    fun refreshAggressive() {
        val cont = container ?: return; val wm = windowManager; val view = floatingView; if (wm == null || view == null) { showAggressiveWindow(); return }; cont.removeAllViews(); if (NotificationRepository.notifs.isEmpty()) { view.visibility = View.GONE; disableKeyboard(); return }; view.visibility = View.VISIBLE; val ctx = themedContext ?: this; val inflater = LayoutInflater.from(ctx)
        for (notif in NotificationRepository.notifs.toList()) {
            val card = inflater.inflate(R.layout.item_notification_float, cont, false)
            val iconView = card.findViewById<android.widget.ImageView>(R.id.notifIcon); val appNameView = card.findViewById<TextView>(R.id.notifAppName); val timeView = card.findViewById<TextView>(R.id.notifTime); val titleView = card.findViewById<TextView>(R.id.notifTitle); val contentView = card.findViewById<TextView>(R.id.notifContent); val actionsContainer = card.findViewById<LinearLayout>(R.id.notifActions)
            try { iconView.setImageDrawable(packageManager.getApplicationIcon(notif.packageName)) } catch (_: Exception) {}
            appNameView.text = notif.appName; timeView.text = android.text.format.DateFormat.format("HH:mm", notif.time).toString(); titleView.text = if (notif.title.isBlank()) notif.appName else notif.title; contentView.text = notif.content; actionsContainer.removeAllViews()
            fun addBtn(label: String, color: Int, onClick: () -> Unit) { val btn = TextView(ctx).apply { text = label; setTextColor(color); textSize = 13f; setPadding(24,16,24,16); isClickable = true; isFocusable = true; setOnClickListener { onClick() } }; actionsContainer.addView(btn) }
            addBtn("FERMER", Color.parseColor("#FFE53935")) { NotificationRepository.notifs.remove(notif); try { NotificationListener.getInstance()?.cancelNotification(notif.sbnKey) } catch (_: Exception) {}; refreshAggressive() }
            notif.notification.actions?.forEach { action -> val title = action.title.toString(); val isReply = action.remoteInputs != null && action.remoteInputs.isNotEmpty(); if (!isReply) { addBtn(title.uppercase(), Color.parseColor("#FF1565C0")) { try { action.actionIntent.send() } catch (_: Exception) {}; NotificationRepository.notifs.remove(notif); refreshAggressive() } } }
            addBtn("OUVRIR", Color.parseColor("#FF1565C0")) { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if (launch != null) startActivity(launch) else notif.notification.contentIntent?.send() } catch (_: Exception) {}; NotificationRepository.notifs.remove(notif); refreshAggressive() }
            val directReplyAction = notif.notification.actions?.firstOrNull { it.remoteInputs != null && it.remoteInputs.isNotEmpty() }
            addBtn("REPONDRE", Color.BLACK) {
                actionsContainer.removeAllViews()
                val inputLayout = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0,8,0,0) }
                val edit = EditText(ctx).apply { hint = "Repondre..."; setHintTextColor(Color.parseColor("#FF999999")); setTextColor(Color.BLACK); setBackgroundResource(R.drawable.white_rounded_bg); layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 12 }; setPadding(24,20,24,20) }
                val sendBtn = TextView(ctx).apply { text = "ENVOYER"; setTextColor(Color.BLACK); textSize = 14f; setPadding(24,16,24,16); isClickable = true }
                sendBtn.setOnClickListener {
                    val replyText = edit.text.toString(); if (replyText.isBlank()) return@setOnClickListener
                    if (directReplyAction != null) { try { val remoteInputs = directReplyAction.remoteInputs; val results = android.os.Bundle(); for (ri in remoteInputs) { results.putCharSequence(ri.resultKey, replyText) }; val fillIntent = Intent(); android.app.RemoteInput.addResultsToIntent(remoteInputs, fillIntent, results); directReplyAction.actionIntent.send(this, 0, fillIntent); Toast.makeText(this, "Reponse envoyee", Toast.LENGTH_SHORT).show() } catch (e: Exception) {} } else { try { val launch = packageManager.getLaunchIntentForPackage(notif.packageName); launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if (launch != null) startActivity(launch) else notif.notification.contentIntent?.send() } catch (_: Exception) {} }
                    disableKeyboard(); NotificationRepository.notifs.remove(notif); try { NotificationListener.getInstance()?.cancelNotification(notif.sbnKey) } catch (_: Exception) {}; refreshAggressive()
                }
                inputLayout.addView(edit); inputLayout.addView(sendBtn); actionsContainer.addView(inputLayout); edit.post { enableKeyboard(edit) }
            }
            cont.addView(card)
        }
    }
    private fun removeAggressiveWindow() { try { floatingView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}; floatingView = null; container = null }
}
