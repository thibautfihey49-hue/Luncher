package com.luncher
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executors
class LauncherActivity : AppCompatActivity() {
    private lateinit var recyclerApps: RecyclerView
    private lateinit var searchBar: EditText
    private var allApps: List<AppInfo> = emptyList()
    private val bgExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val packageReceiver = object: BroadcastReceiver(){ override fun onReceive(c: Context?, i: Intent?){ loadAppsFast() } }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        recyclerApps = findViewById(R.id.recyclerApps) ?: return
        searchBar = findViewById(R.id.searchBar) ?: return
        recyclerApps.layoutManager = GridLayoutManager(this, 4)
        recyclerApps.setHasFixedSize(true); recyclerApps.itemAnimator = null; recyclerApps.setItemViewCacheSize(20)
        val adapter = AppAdapter(mutableListOf()) { app -> try{ startActivity(packageManager.getLaunchIntentForPackage(app.packageName)) }catch(_:Exception){} }
        recyclerApps.adapter = adapter
        findViewById<View>(R.id.clearSearch)?.setOnClickListener { searchBar.text.clear() }
        searchBar.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ val q=s.toString(); if(q.isBlank()) adapter.update(allApps) else adapter.update(allApps.filter{ it.label.contains(q,true) }) }
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
        findViewById<View>(R.id.btnPhone)?.setOnClickListener { startActivity(Intent(this, PhoneAppActivity::class.java)) }
        findViewById<View>(R.id.btnSms)?.setOnClickListener { startActivity(Intent(this, SmsAppActivity::class.java)) }
        findViewById<View>(R.id.btnFiles)?.setOnClickListener { startActivity(Intent(this, FileManagerActivity::class.java)) }
        val filter = IntentFilter().apply{ addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED); addDataScheme("package") }
        try{ registerReceiver(packageReceiver, filter) }catch(_:Exception){}
        loadAppsFast()
    }
    private fun loadAppsFast(){
        bgExecutor.execute{
            try{
                val pm=packageManager; val intent=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
                val apps=pm.queryIntentActivities(intent,0).mapNotNull{ try{ val label=it.loadLabel(pm).toString(); val pkg=it.activityInfo.packageName; if(pkg==packageName) null else AppInfo(label,pkg,it.activityInfo.name,it.loadIcon(pm)) }catch(_:Exception){ null } }.sortedBy{ it.label.lowercase() }
                mainHandler.post{ allApps=apps; (recyclerApps.adapter as? AppAdapter)?.update(apps) }
            }catch(_:Exception){}
        }
    }
    override fun onDestroy(){ super.onDestroy(); try{ unregisterReceiver(packageReceiver) }catch(_:Exception){}; bgExecutor.shutdown() }
}
