package com.luncher.ui; import android.app.*; import android.content.*; import android.graphics.PixelFormat; import android.os.Build; import android.os.IBinder; import android.view.*; import android.widget.*; import com.luncher.R; import com.luncher.data.MessageItem; import kotlinx.coroutines.*
class FloatingWindowService : Service() {
    private lateinit var wm: WindowManager; private var view: View?=null; private var instance: FloatingWindowService?=null
    companion object { fun showMessage(ctx: Context, m: MessageItem) = instance?.showIt(m) }
    override fun onCreate() { super.onCreate(); wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager; instance=this }
    override fun onBind(i: Intent?): IBinder?=null
    override fun onStartCommand(i:Intent?,f:Int,id:Int):Int { instance=this; return START_STICKY }
    private fun showIt(m: MessageItem) { if(view!=null)return; val l=LayoutInflater.from(this).inflate(R.layout.window_floating_notification,null)
        val t=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val p=WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,t,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT)
        p.gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; p.y=40
        l.findViewById<TextView>(R.id.notif_app).text=m.appName; l.findViewById<TextView>(R.id.notif_sender).text=m.sender
        l.findViewById<TextView>(R.id.notif_content).text=m.content; l.findViewById<TextView>(R.id.notif_time).text=m.time
        l.findViewById<ImageView>(R.id.notif_icon).setImageResource(m.icon)
        l.findViewById<View>(R.id.btn_close).setOnClickListener{closeView()}
        l.findViewById<View>(R.id.btn_open).setOnClickListener{packageManager.getLaunchIntentForPackage(m.packageName)?.let{startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));closeView()}}
        l.findViewById<View>(R.id.btn_reply).setOnClickListener{Toast.makeText(this,"✉️ Réponse envoyée",Toast.LENGTH_SHORT).show();l.findViewById<EditText>(R.id.et_reply).text.clear()}
        view=l; wm.addView(l,p); CoroutineScope(Dispatchers.Main).launch{delay(10000);closeView()}
    }
    private fun closeView() { view?.let{wm.removeView(it)}; view=null }
    override fun onDestroy() { super.onDestroy(); closeView(); instance=null }
}
