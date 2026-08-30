package com.luncher.data
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.NotificationCompat
import com.luncher.R
class FloatingNotificationService : Service() {
    private var wm: WindowManager? = null
    private var root: View? = null
    private var container: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null
    companion object {
        var instance: FloatingNotificationService? = null
        fun show(c: Context){ val i=Intent(c,FloatingNotificationService::class.java); if(Build.VERSION.SDK_INT>=26) c.startForegroundService(i) else c.startService(i) }
        fun hide(c: Context){ c.stopService(Intent(c,FloatingNotificationService::class.java)) }
        fun forceRefresh(){ instance?.refreshAggressive() }
    }
    override fun onCreate(){ super.onCreate(); instance=this; wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager; if(Build.VERSION.SDK_INT>=26){ val ch=NotificationChannel("luncher_aggressive","Luncher",NotificationManager.IMPORTANCE_LOW); (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch) }; startForeground(1001, NotificationCompat.Builder(this,"luncher_aggressive").setContentTitle("Luncher").setSmallIcon(R.drawable.ic_launcher_foreground).build()); showAggressiveWindow(); NotificationRepository.listener={refreshAggressive()} }
    override fun onStartCommand(i: Intent?, f:Int, s:Int):Int{ showAggressiveWindow(); refreshAggressive(); return START_STICKY }
    override fun onDestroy(){ try{root?.let{wm?.removeView(it)}}catch(_:Exception){}; NotificationRepository.listener=null; instance=null; super.onDestroy(); try{show(this)}catch(_:Exception){} }
    override fun onBind(i: Intent?)=null
    private fun showAggressiveWindow(){ if(root!=null) try{wm?.removeView(root)}catch(_:Exception){}; val scroll=ScrollView(this); val cont=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(8,8,8,8)}; scroll.addView(cont); container=cont; val p=WindowManager.LayoutParams(-1,-2,if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP; y=80; softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE}; params=p; root=scroll; try{wm?.addView(root,p)}catch(_:Exception){}; refreshAggressive() }
    private fun enableKb(e: EditText){ try{ params?.let{ it.flags=it.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv(); wm?.updateViewLayout(root,it) }; e.isFocusable=true; e.isFocusableInTouchMode=true; e.requestFocus(); (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(e,0)}catch(_:Exception){} }
    private fun disableKb(){ try{ params?.let{ it.flags=it.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; wm?.updateViewLayout(root,it) }; (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root?.windowToken,0)}catch(_:Exception){} }

    fun refreshAggressive(){
        val cont=container?:return; if(wm==null||root==null){ showAggressiveWindow(); return }; cont.removeAllViews(); if(NotificationRepository.notifs.isEmpty()){ root?.visibility=View.GONE; disableKb(); return }; root?.visibility=View.VISIBLE; val inf=LayoutInflater.from(this)
        for(notif in NotificationRepository.notifs.toList()){
            if(notif.packageName=="android") continue // on cache le "Luncher se superpose"
            val card=inf.inflate(R.layout.item_notification_float,cont,false)
            card.findViewById<TextView>(R.id.notifAppName).text="${notif.appName} - ${notif.title}"
            card.findViewById<TextView>(R.id.notifContent).apply{text=notif.content.ifBlank{"${notif.title}"}; maxLines=15}
            card.findViewById<TextView>(R.id.notifTitle).text="${notif.notification.actions?.size?:0} actions"
            val actionsBox=card.findViewById<LinearLayout>(R.id.notifActions); actionsBox.removeAllViews()
            fun btn(txt:String,col:Int,click:()->Unit){ val b=TextView(this).apply{text=txt; setTextColor(col); textSize=14f; setPadding(24,24,24,24); setBackgroundColor(Color.parseColor("#FFEEEEEE")); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,6,0,6)}; setOnClickListener{click()}}; actionsBox.addView(b) }

            val replyAction = notif.notification.actions?.firstOrNull{ it.remoteInputs!=null && it.remoteInputs.isNotEmpty() }

            fun showReplyUI(){
                actionsBox.removeAllViews()
                val ll=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setPadding(0,12,0,0)}
                val ed=EditText(this).apply{hint="Tape ta réponse..."; setTextColor(Color.BLACK); setBackgroundColor(Color.WHITE); setPadding(20,20,20,20); layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
                val send=TextView(this).apply{text=" ENVOYER "; setTextColor(Color.WHITE); setBackgroundColor(Color.BLACK); setPadding(28,22,28,22); textSize=14f}
                send.setOnClickListener{
                    val txt=ed.text.toString().trim(); if(txt.isEmpty()) return@setOnClickListener
                    try{
                        if(replyAction!=null){
                            val results=android.os.Bundle()
                            for(ri in replyAction.remoteInputs){ results.putCharSequence(ri.resultKey, txt) }
                            val fillIn=Intent()
                            android.app.RemoteInput.addResultsToIntent(replyAction.remoteInputs, fillIn, results)
                            replyAction.actionIntent.send(this,0,fillIn)
                            Toast.makeText(this,"Réponse envoyée ✓",0).show()
                        }
                    }catch(e:Exception){ Toast.makeText(this,"Erreur: ${e.message}",1).show() }
                    disableKb(); NotificationRepository.notifs.remove(notif); try{NotificationListener.getInstance()?.cancelNotif(notif.sbnKey)}catch(_:Exception){}; refreshAggressive()
                }
                ll.addView(ed); ll.addView(send); actionsBox.addView(ll)
                ed.postDelayed({enableKb(ed)},150)
            }

            btn("FERMER", Color.RED){ NotificationRepository.notifs.remove(notif); try{NotificationListener.getInstance()?.cancelNotif(notif.sbnKey)}catch(_:Exception){}; refreshAggressive() }

            notif.notification.actions?.forEach{ a->
                val isReply = a.remoteInputs!=null && a.remoteInputs.isNotEmpty()
                val title = try{a.title?.toString() ?: "ACTION"}catch(_:Exception){"ACTION"}
                if(isReply){
                    // intercepte le Répondre natif -> ouvre notre champ
                    btn(title, Color.parseColor("#FF0000FF")){ showReplyUI() }
                } else {
                    btn(title, Color.parseColor("#FF1565C0")){ try{a.actionIntent.send()}catch(_:Exception){}; NotificationRepository.notifs.remove(notif); refreshAggressive() }
                }
            }

            btn("OUVRIR", Color.parseColor("#FF333333")){ try{val i=packageManager.getLaunchIntentForPackage(notif.packageName); i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i)}catch(_:Exception){}; NotificationRepository.notifs.remove(notif); refreshAggressive() }

            if(replyAction!=null){
                btn("REPONDRE", Color.BLACK){ showReplyUI() }
            }

            cont.addView(card)
        }
    }
}
