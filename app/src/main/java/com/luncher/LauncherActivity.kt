package com.luncher
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
    private var clearSearch: TextView? = null
    private lateinit var appAdapter: AppAdapter
    private var allApps: List<AppInfo> = emptyList()
    private val bgExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastLoad = 0L

    private val packageReceiver = object: BroadcastReceiver(){
        override fun onReceive(c: Context?, intent: Intent?) {
            if(intent?.action in listOf(Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_CHANGED)){
                debouncedLoad()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        recyclerApps = findViewById(R.id.recyclerApps)
        searchBar = findViewById(R.id.searchBar)
        clearSearch = findViewById(R.id.clearSearch) as? TextView

        // ULTRA OPTIM RECYCLER
        recyclerApps.layoutManager = GridLayoutManager(this, 4)
        recyclerApps.setHasFixedSize(true)
        recyclerApps.setItemViewCacheSize(20)
        recyclerApps.isDrawingCacheEnabled = true
        recyclerApps.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
        recyclerApps.itemAnimator = null // Pas d'anim = plus rapide

        appAdapter = AppAdapter(mutableListOf()) { app -> launchApp(app) }
        recyclerApps.adapter = appAdapter

        clearSearch?.setOnClickListener { searchBar.text.clear() }
        searchBar.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
            override fun onTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
            override fun afterTextChanged(s:Editable?){
                val q = s?.toString()?: ""
                clearSearch?.visibility = if(q.isNotEmpty()) View.VISIBLE else View.GONE
                if(q.length>=1) filterFast(q) else appAdapter.update(allApps)
            }
        })

        findViewById<View>(R.id.btnPhone)?.setOnClickListener { startActivity(Intent(this, PhoneAppActivity::class.java)) }
        findViewById<View>(R.id.btnSms)?.setOnClickListener { startActivity(Intent(this, SmsAppActivity::class.java)) }
        findViewById<View>(R.id.btnFiles)?.setOnClickListener { startActivity(Intent(this, FileManagerActivity::class.java)) }

        // Receiver que si app install/suppr
        val filter = IntentFilter().apply{
            addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED); addAction(Intent.ACTION_PACKAGE_CHANGED); addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)

        loadAppsFast()
    }

    override fun onDestroy() { super.onDestroy(); try{ unregisterReceiver(packageReceiver) }catch(_:Exception){}; bgExecutor.shutdown() }

    override fun onResume(){
        super.onResume()
        // Reload seulement si >30sec depuis dernier load pour économiser batterie
        if(System.currentTimeMillis() - lastLoad > 30000) loadAppsFast()
    }

    private fun debouncedLoad(){
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({ loadAppsFast() }, 500)
    }

    private fun loadAppsFast(){
        bgExecutor.execute{
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
            val list = pm.queryIntentActivities(mainIntent, 0)
            val apps = if(list.isNotEmpty()){
                list.mapNotNull{
                    try{
                        val label = it.loadLabel(pm).toString()
                        val pkg = it.activityInfo.packageName
                        if(pkg==packageName) null else AppInfo(label, pkg, it.activityInfo.name, it.loadIcon(pm))
                    }catch(_:Exception){ null }
                }
            }else emptyList()
            val sorted = apps.sortedBy{ it.label.lowercase() }
            mainHandler.post{
                allApps = sorted
                appAdapter.update(sorted)
                lastLoad = System.currentTimeMillis()
            }
        }
    }

    private fun filterFast(q:String){
        bgExecutor.execute{
            val f = allApps.filter{ it.label.contains(q,true) }
            mainHandler.post{ appAdapter.update(f) }
        }
    }

    private fun launchApp(app: AppInfo){
        try{
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)?.apply{ addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) }
            startActivity(intent)
        }catch(e:Exception){}
    }
}
