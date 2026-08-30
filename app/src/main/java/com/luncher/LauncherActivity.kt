package com.luncher
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.View
import android.widget.EditText

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_launcher)
            initRealLauncher()
        } catch(e: Exception) {
            val tv = TextView(this)
            tv.text = "CRASH: " + (e.message ?: "unknown")
            tv.setPadding(20,100,20,20)
            setContentView(tv)
        }
    }
    private fun initRealLauncher() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
        val search = findViewById<EditText>(R.id.searchBar)
        recycler?.layoutManager = GridLayoutManager(this, 4)
        val adapter = AppAdapter(mutableListOf()) { app ->
            try { startActivity(packageManager.getLaunchIntentForPackage(app.packageName)) } catch(_:Exception){}
        }
        recycler?.adapter = adapter
        Thread {
            try {
                val pm = packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
                val apps = pm.queryIntentActivities(intent, 0).mapNotNull {
                    try {
                        val label = it.loadLabel(pm).toString()
                        val pkg = it.activityInfo.packageName
                        if(pkg == packageName) null else AppInfo(label, pkg, it.activityInfo.name, it.loadIcon(pm))
                    } catch(_:Exception){ null }
                }.sortedBy{ it.label.lowercase() }
                runOnUiThread { try{ adapter.update(apps) }catch(_:Exception){} }
            } catch(_:Exception){}
        }.start()
        findViewById<View>(R.id.btnPhone)?.setOnClickListener { try{ startActivity(Intent(this, PhoneAppActivity::class.java)) }catch(_:Exception){} }
        findViewById<View>(R.id.btnSms)?.setOnClickListener { try{ startActivity(Intent(this, SmsAppActivity::class.java)) }catch(_:Exception){} }
        findViewById<View>(R.id.btnFiles)?.setOnClickListener { try{ startActivity(Intent(this, FileManagerActivity::class.java)) }catch(_:Exception){} }
    }
}