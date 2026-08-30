package com.luncher.data
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.luncher.R
class FloatingNotificationService : Service() {
    private var wm: WindowManager? = null
    private var root: View? = null
    private var container: LinearLayout? = null
    companion object { var instance: FloatingNotificationService? = null
        fun show(c: Context){ val i=Intent(c,FloatingNotificationService::class.java); if(Build.VERSION.SDK_INT>=26) c.startForegroundService(i) else c.startService(i) }
        fun hide(c: Context){ c.stopService(Intent(c,FloatingNotificationService::class.java)) }
        fun forceRefresh(){ instance?.refresh() } }
    override fun onCreate(){ super.onCreate(); instance=this; wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager; val ch=NotificationChannel("luncher_aggressive","Luncher",NotificationManager.IMPORTANCE_LOW); (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch); startForeground(1001, NotificationCompat.Builder(this,"luncher_aggressive").setContentTitle("Luncher").setSmallIcon(R.drawable.ic_launcher_foreground).build()); showWindow(); NotificationRepository.listener={refresh()} }
    override fun onStartCommand(i: Intent?, f:Int, s:Int):Int{ showWindow(); refresh(); return START_STICKY }
    override fun onDestroy(){ try{root?.let{wm?.removeView(it)}}catch(_:Exception){}; instance=null; super.onDestroy() }
    override fun onBind(i: Intent?)=null
    private fun showWindow(){ if(root!=null) try{wm?.removeView(root)}catch(_:Exception){}; val scroll=ScrollView(this); val cont=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; scroll.addView(cont); container=cont; val p=WindowManager.LayoutParams(-1,-2,if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP; y=80}; root=scroll; try{wm?.addView(root,p)}catch(_:Exception){} }
    fun refresh(){
        val cont=container?:return; cont.removeAllViews(); if(NotificationRepository.notifs.isEmpty()){root?.visibility=View.GONE; return}; root?.visibility=View.VISIBLE
        for(notif in NotificationRepository.notifs.toList()){
            val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE); setPadding(30,30,30,20); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(16,16,16,16)}}
            val t1=TextView(this).apply{text=notif.appName+" - "+notif.title; setTextColor(Color.BLACK); textSize=14f; setTypeface(null,android.graphics.Typeface.BOLD)}
            val t2=TextView(this).apply{text=notif.content; setTextColor(Color.DKGRAY); textSize=13f}
            val debug=TextView(this).apply{text="DEBUG actions=${notif.notification.actions?.size} pkg=${notif.packageName}"; setTextColor(Color.RED); textSize=10f}
            val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            fun btn(txt:String, col:Int, click:()->Unit){ val b=TextView(this).apply{text=" [$txt] "; setTextColor(col); textSize=14f; setPadding(20,20,20,20); setBackgroundColor(Color.LTGRAY); setOnClickListener{click()}}; actions.addView(b) }
            btn("FERMER", Color.RED){ NotificationRepository.notifs.remove(notif); refresh() }
            notif.notification.actions?.forEach{ a-> btn(a.title.toString(), Color.BLUE){ try{a.actionIntent.send()}catch(_:Exception){}; NotificationRepository.notifs.remove(notif); refresh() } }
            btn("OUVRIR", Color.BLUE){ try{ packageManager.getLaunchIntentForPackage(notif.packageName)?.let{ startActivity(it.apply{addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)}) } }catch(_:Exception){}; NotificationRepository.notifs.remove(notif); refresh() }
            btn("REPONDRE", Color.BLACK){ Toast.makeText(this,"REPONDRE clique - direct=${notif.notification.actions?.any{it.remoteInputs!=null}}",1).show(); try{ packageManager.getLaunchIntentForPackage(notif.packageName)?.let{ startActivity(it.apply{addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)}) } }catch(_:Exception){} }
            card.addView(t1); card.addView(t2); card.addView(debug); card.addView(actions); cont.addView(card)
        }
    }
}
