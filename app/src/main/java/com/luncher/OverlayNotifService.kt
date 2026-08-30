package com.luncher
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
class OverlayNotifService : Service() {
    private var wm: WindowManager? = null
    private var view: android.view.View? = null
    companion object {
        fun showCall(context: Context, number: String){
            val i = Intent(context, OverlayNotifService::class.java).apply{ putExtra("type","call"); putExtra("number", number) }
            if(Build.VERSION.SDK_INT>=26) context.startForegroundService(i) else context.startService(i)
        }
        fun showSms(context: Context, from: String, body: String){
            val i = Intent(context, OverlayNotifService::class.java).apply{ putExtra("type","sms"); putExtra("from", from); putExtra("body", body) }
            if(Build.VERSION.SDK_INT>=26) context.startForegroundService(i) else context.startService(i)
        }
    }
    override fun onCreate(){
        super.onCreate(); wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if(Build.VERSION.SDK_INT>=26){
            val ch = NotificationChannel("overlay", "Overlay", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch)
        }
        startForeground(2001, NotificationCompat.Builder(this,"overlay").setContentTitle("Luncher").setSmallIcon(R.drawable.ic_launcher_foreground).build())
    }
    override fun onStartCommand(intent: Intent?, f:Int, s:Int): Int {
        val type = intent?.getStringExtra("type")?: return START_NOT_STICKY
        showOverlay(type, intent); return START_STICKY
    }
    private fun showOverlay(type:String, intent: Intent){
        try{ view?.let{ wm?.removeView(it) } }catch(_:Exception){}
        val layout = android.widget.LinearLayout(this).apply{
            orientation = android.widget.LinearLayout.VERTICAL; setPadding(32,32,32,32)
            background = android.graphics.drawable.GradientDrawable().apply{ setColor(Color.WHITE); cornerRadius=32f }
        }
        val title = TextView(this).apply{ textSize=16f; setTextColor(Color.BLACK); text = if(type=="call") "📞 Appel entrant" else "💬 Nouveau message"; setTypeface(null, android.graphics.Typeface.BOLD) }
        val sub = TextView(this).apply{ textSize=15f; setTextColor(Color.parseColor("#222222")); setPadding(0,12,0,12); text = if(type=="call") intent.getStringExtra("number") else "${intent.getStringExtra("from")}\n${intent.getStringExtra("body")}" }
        val row = android.widget.LinearLayout(this).apply{ orientation=android.widget.LinearLayout.HORIZONTAL }
        val btnOpen = TextView(this).apply{ text="Ouvrir"; setTextColor(Color.WHITE); setBackgroundColor(Color.BLACK); setPadding(32,18,32,18); gravity=Gravity.CENTER }
        val btnClose = TextView(this).apply{ text="Fermer"; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#F0F0F0")); setPadding(32,18,32,18); gravity=Gravity.CENTER }
        btnOpen.setOnClickListener{ if(type=="call") startActivity(Intent(this, PhoneAppActivity::class.java).apply{ addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) else startActivity(Intent(this, SmsAppActivity::class.java).apply{ addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }); hideOverlay() }
        btnClose.setOnClickListener{ hideOverlay() }
        row.addView(btnOpen, android.widget.LinearLayout.LayoutParams(0, -2, 1f).apply{ setMargins(0,0,8,0) })
        row.addView(btnClose, android.widget.LinearLayout.LayoutParams(0, -2, 1f).apply{ setMargins(8,0,0,0) })
        layout.addView(title); layout.addView(sub); layout.addView(row)
        val params = WindowManager.LayoutParams(-1, -2, if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply{ gravity=Gravity.TOP; y=80 }
        view=layout
        try{ wm?.addView(view, params) }catch(e:Exception){}
        android.os.Handler(mainLooper).postDelayed({ hideOverlay() }, 6000)
    }
    private fun hideOverlay(){ try{ view?.let{ wm?.removeView(it) }; view=null }catch(_:Exception){}; stopSelf() }
    override fun onDestroy(){ try{ view?.let{ wm?.removeView(it) } }catch(_:Exception){}; super.onDestroy() }
    override fun onBind(i: Intent?)=null
}
