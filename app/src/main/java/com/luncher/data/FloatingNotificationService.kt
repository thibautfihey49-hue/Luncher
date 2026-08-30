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
    companion object { var instance: FloatingNotificationService? = null
        fun show(c: Context){ val i=Intent(c,FloatingNotificationService::class.java); if(Build.VERSION.SDK_INT>=26) c.startForegroundService(i) else c.startService(i) }
        fun hide(c: Context){ c.stopService(Intent(c,FloatingNotificationService::class.java)) }
        fun forceRefresh(){ instance?.refresh() } }
    override fun onCreate(){ super.onCreate(); instance=this; wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager; val ch=NotificationChannel("luncher_aggressive","Luncher",NotificationManager.IMPORTANCE_LOW); (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch); startForeground(1001, NotificationCompat.Builder(this,"luncher_aggressive").setContentTitle("Luncher").setSmallIcon(R.drawable.ic_launcher_foreground).build()); showWindow(); NotificationRepository.listener={refresh()} }
    override fun onStartCommand(i: Intent?, f:Int, s:Int):Int{ showWindow(); refresh(); return START_STICKY }
    override fun onDestroy(){ try{root?.let{wm?.removeView(it)}}catch(_:Exception){}; instance=null; super.onDestroy() }
    override fun onBind(i: Intent?)=null
    private fun showWindow(){ if(root!=null) try{wm?.removeView(root)}catch(_:Exception){}; val scroll=ScrollView(this); val cont=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; scroll.addView(cont); container=cont; val p=WindowManager.LayoutParams(-1,-2,if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP; y=80; softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE}; params=p; root=scroll; try{wm?.addView(root,p)}catch(_:Exception){} }
    private fun enableKb(e: EditText){ try{ params?.let{ it.flags=it.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv(); wm?.updateViewLayout(root,it) }; e.requestFocus(); (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(e,0)}catch(_:Exception){} }
    private fun disableKb(){ try{ params?.let{ it.flags=it.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; wm?.updateViewLayout(root,it) }; (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root?.windowToken,0)}catch(_:Exception){} }
    fun refresh(){
        val cont=container?:return; cont.removeAllViews(); if(NotificationRepository.notifs.isEmpty()){root?.visibility=View.GONE; disableKb(); return}; root?.visibility=View.VISIBLE
        for(notif in NotificationRepository.notifs.toList()){
            val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE); setPadding(30,30,30,20); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(16,16,16,16)}}
            val t1=TextView(this).apply{text=notif.appName+" | "+notif.title; setTextColor(Color.BLACK); setTypeface(null,android.graphics.Typeface.BOLD)}
            val t2=TextView(this).apply{text=notif.content; setTextColor(Color.DKGRAY)}
            val debug=TextView(this).apply{text="actions=${notif.notification.actions?.size} hasRemote=${notif.notification.actions?.any{it.remoteInputs!=null}}"; setTextColor(Color.RED); textSize=11f}
            val actions=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
            fun btn(txt:String,col:Int,click:()->Unit){ val b=TextView(this).apply{text=txt; setTextColor(col); textSize=15f; setPadding(30,30,30,30); setBackgroundColor(Color.parseColor("#FFEEEEEE")); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,4,0,4)}; setOnClickListener{click()}}; actions.addView(b) }
            btn("FERMER", Color.RED){ NotificationRepository.notifs.remove(notif); refresh() }
            notif.notification.actions?.forEachIndexed{ idx, a->
                val title = try{ a.title?.toString() ?: "ACTION $idx" }catch(_:Exception){ "ACTION $idx" }
                val isReply = a.remoteInputs!=null && a.remoteInputs.isNotEmpty()
                btn(if(isReply) "$title (REPLY)" else title, Color.BLUE){
                    try{
                        if(isReply){ Toast.makeText(this,"REPLY direct dispo",0).show() } else a.actionIntent.send()
                    }catch(_:Exception){}
                    NotificationRepository.notifs.remove(notif); refresh()
                }
            }
            btn("OUVRIR GMAIL", Color.parseColor("#FF1565C0")){ try{ val i=packageManager.getLaunchIntentForPackage(notif.packageName); i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) }catch(_:Exception){}; NotificationRepository.notifs.remove(notif); refresh() }
            val direct=notif.notification.actions?.firstOrNull{ it.remoteInputs!=null && it.remoteInputs.isNotEmpty() }
            btn("REPONDRE DIRECT (CLAVIER)", Color.BLACK){
                actions.removeAllViews()
                val ll=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setPadding(0,20,0,0)}
                val ed=EditText(this).apply{hint="Repondre..."; setTextColor(Color.BLACK); setBackgroundColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
                val send=TextView(this).apply{text=" ENVOYER "; setTextColor(Color.WHITE); setBackgroundColor(Color.BLACK); setPadding(40,20,40,20)}
                send.setOnClickListener{
                    val txt=ed.text.toString(); if(txt.isBlank()) return@setOnClickListener
                    if(direct!=null){ try{ val b=android.os.Bundle(); for(ri in direct.remoteInputs){ b.putCharSequence(ri.resultKey,txt) }; val fi=Intent(); android.app.RemoteInput.addResultsToIntent(direct.remoteInputs,fi,b); direct.actionIntent.send(this,0,fi); Toast.makeText(this,"Envoye a Gmail",0).show() }catch(e:Exception){ Toast.makeText(this,"Erreur ${e.message}",1).show() } } else Toast.makeText(this,"Pas de RemoteInput",0).show()
                    disableKb(); NotificationRepository.notifs.remove(notif); refresh()
                }
                ll.addView(ed); ll.addView(send); actions.addView(ll); ed.post{enableKb(ed)}
            }
            card.addView(t1); card.addView(t2); card.addView(debug); card.addView(actions); cont.addView(card)
        }
    }
}
