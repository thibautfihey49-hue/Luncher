package com.luncher
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
class LauncherActivity : AppCompatActivity() {
    private lateinit var recyclerApps: RecyclerView
    private lateinit var searchBar: EditText
    private var clearSearch: TextView? = null
    private lateinit var appAdapter: AppAdapter
    private var allApps: List<AppInfo> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        recyclerApps = findViewById(R.id.recyclerApps)
        searchBar = findViewById(R.id.searchBar)
        clearSearch = findViewById(R.id.clearSearch) as? TextView
        recyclerApps.layoutManager = GridLayoutManager(this, 4)
        recyclerApps.setHasFixedSize(true)
        recyclerApps.setItemViewCacheSize(40)
        appAdapter = AppAdapter(mutableListOf()) { app -> launchApp(app) }
        recyclerApps.adapter = appAdapter
        clearSearch?.setOnClickListener { searchBar.text.clear() }
        searchBar.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
            override fun onTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
            override fun afterTextChanged(s:Editable?){
                val q = s?.toString()?: ""
                clearSearch?.visibility = if(q.isNotEmpty()) View.VISIBLE else View.GONE
                filter(q)
            }
        })
        findViewById<View>(R.id.btnPhone)?.setOnClickListener { startActivity(Intent(this, PhoneAppActivity::class.java)) }
        findViewById<View>(R.id.btnSms)?.setOnClickListener { startActivity(Intent(this, SmsAppActivity::class.java)) }
        findViewById<View>(R.id.btnFiles)?.setOnClickListener { startActivity(Intent(this, FileManagerActivity::class.java)) }
        loadApps()
    }
    override fun onResume(){ super.onResume(); loadApps() }
    private fun loadApps(){
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
        val apps = pm.queryIntentActivities(mainIntent, 0).map{
            AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.activityInfo.name, it.loadIcon(pm))
        }.sortedBy{ it.label.lowercase() }.filterNot{ it.packageName==packageName }
        allApps = apps
        appAdapter.update(allApps)
    }
    private fun filter(q:String){
        val f = if(q.isBlank()) allApps else allApps.filter{ it.label.contains(q,true) || it.packageName.contains(q,true) }
        appAdapter.update(f)
    }
    private fun launchApp(app: AppInfo){
        try{
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)?: Intent().apply{ setClassName(app.packageName, app.className); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            startActivity(intent)
        }catch(e:Exception){}
    }
}
data class AppInfo(val label:String, val packageName:String, val className:String, val icon:android.graphics.drawable.Drawable)
