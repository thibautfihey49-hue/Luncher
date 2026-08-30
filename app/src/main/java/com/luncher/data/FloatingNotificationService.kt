package com.luncher.data
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
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
    companion object { var instance: FloatingNotificationService? = null; fun show(c: Context){ val i=Intent(c,FloatingNotificationService::class.java); if(Build.VERSION.SDK_INT>=26) c.startForegroundService(i) else c.startService(i) }; fun forceRefresh(){ instance?.refreshAggressive() } }
    override fun onCreate(){ super.onCreate(); instance=this; wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager; if(Build.VERSION.SDK_INT>=26){ val ch=NotificationChannel("luncher_aggressive","Luncher",NotificationManager.IMPORTANCE_LOW); (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch) }; startForeground(1001, NotificationCompat.Builder(this,"luncher_aggressive").setContentTitle("Luncher").setSmallIcon(R.drawable.ic_launcher_foreground).build()); showWindow(); NotificationRepository.listener={refreshAggressive()} }
    override fun onStartCommand(i: Intent?, f:Int, s:Int):Int{ showWindow(); refreshAggressive(); return START_STICKY }
    override fun onDestroy(){ try{root?.let{wm?.removeView(it)}}catch(_:Exception){}; NotificationRepository.listener=null; instance=null; super.onDestroy() }
    override fun onBind(i: Intent?)=null
    private fun showWindow(){ if(root!=null) try{wm?.removeView(root)}catch(_:Exception){}; val scroll=ScrollView(this).apply{isVerticalScrollBarEnabled=false}; val cont=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(16,16,16,16)}; scroll.addView(cont); container=cont; val p=WindowManager.LayoutParams(-1,-2,if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP; y=90; softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE}; params=p; root=scroll; try{wm?.addView(root,p)}catch(_:Exception){} }
    private fun enableKb(e: EditText){ try{ params!!.flags=params!!.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv(); wm?.updateViewLayout(root,params); e.requestFocus(); (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(e,0)}catch(_:Exception){} }
    private fun disableKb(){ try{ params!!.flags=params!!.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; wm?.updateViewLayout(root,params); (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root?.windowToken,0)}catch(_:Exception){} }
    private fun roundedBg(color:Int, radius:Float): GradientDrawable { return GradientDrawable().apply{ setColor(color); cornerRadius=radius } }
    fun refreshAggressive(){
        val cont=container?:return; if(wm==null||root==null){ showWindow(); return }; cont.removeAllViews(); if(NotificationRepository.notifs.isEmpty()){ root?.visibility=View.GONE; disableKb(); return }; root?.visibility=View.VISIBLE; val inf=LayoutInflater.from(this)
        for(notif in NotificationRepository.notifs.toList()){
            if(notif.packageName=="android") continue
            val card=inf.inflate(R.layout.item_notification_float,cont,false)
            card.findViewById<TextView>(R.id.notifAppName).text="${notif.appName} • ${notif.title}"
            try{ card.findViewById<TextView>(R.id.notifTime).text="maintenant" }catch(_:Exception){}
            card.findViewById<TextView>(R.id.notifContent).text=notif.content
            val actionsBox=card.findViewById<LinearLayout>(R.id.notifActions); actionsBox.removeAllViews()
            val replyAction = notif.notification.actions?.firstOrNull{ it.remoteInputs!=null && it.remoteInputs.isNotEmpty() }
            
            // Champ réponse design
            val replyContainer = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(0,14,0,0)}
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; background=roundedBg(Color.parseColor("#FFF2F2F2"), 32f); setPadding(6,6,6,6)}
            val ed=EditText(this).apply{hint="Répondre..."; setHintTextColor(Color.parseColor("#FF999999")); setTextColor(Color.BLACK); background=null; setPadding(18,12,18,12); layoutParams=LinearLayout.LayoutParams(0,-2,1f); maxLines=4; textSize=15f}
            val send=TextView(this).apply{text="↑"; textSize=18f; setTextColor(Color.WHITE); background=roundedBg(Color.BLACK, 60f); setPadding(28,12,28,12); gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(-2,-2).apply{setMargins(6,0,0,0)}}
            row.addView(ed); row.addView(send)
            replyContainer.addView(row)
            actionsBox.addView(replyContainer)
            ed.setOnClickListener{ enableKb(ed) }
            ed.setOnFocusChangeListener{ _,h-> if(h) enableKb(ed) }
            send.setOnClickListener{
                val txt=ed.text.toString().trim(); if(txt.isEmpty()) return@setOnClickListener
                var sent=false
                try{
                    if(replyAction!=null){
                        val b=android.os.Bundle()
                        for(ri in replyAction.remoteInputs) b.putCharSequence(ri.resultKey, txt)
                        val fill=Intent()
                        android.app.RemoteInput.addResultsToIntent(replyAction.remoteInputs, fill, b)
                        replyAction.actionIntent.send(this,0,fill)
                        sent=true
                    }
                }catch(_:Exception){}
                if(sent){
                    Toast.makeText(this,"✓ Envoyé",0).show()
                    disableKb()
                    Handler(Looper.getMainLooper()).postDelayed({
                        NotificationRepository.notifs.remove(notif)
                        try{NotificationListener.getInstance()?.cancelNotif(notif.sbnKey)}catch(_:Exception){}
                        refreshAggressive()
                    }, 700)
                }else{
                    Toast.makeText(this,"Réponse impossible sur ce mail à soi-même",0).show()
                }
            }
            // Boutons secondaires plus discrets
            val secondaryRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setPadding(0,12,0,0)}
            fun miniBtn(t:String, col:Int, cl:()->Unit): TextView {
                return TextView(this).apply{text=t; setTextColor(col); textSize=13f; typeface=android.graphics.Typeface.DEFAULT_BOLD; background=roundedBg(Color.parseColor("#FFEFEFEF"), 20f); setPadding(20,10,20,10); layoutParams=LinearLayout.LayoutParams(-2,-2).apply{setMargins(0,0,10,0)}; setOnClickListener{cl()}}
            }
            secondaryRow.addView(miniBtn("Fermer", Color.parseColor("#FFCC0000")){ NotificationRepository.notifs.remove(notif); try{NotificationListener.getInstance()?.cancelNotif(notif.sbnKey)}catch(_:Exception){}; refreshAggressive() })
            secondaryRow.addView(miniBtn("Ouvrir", Color.parseColor("#FF444444")){ try{ val i=packageManager.getLaunchIntentForPackage(notif.packageName); i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i)}catch(_:Exception){} })
            actionsBox.addView(secondaryRow)
            cont.addView(card)
        }
    }
}
